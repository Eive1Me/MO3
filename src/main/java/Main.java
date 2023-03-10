import mathelement.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        InequalitySystem inequalitySystem = new InequalitySystem();
        Utils.addData(inequalitySystem);
        System.out.println("Изначальная система неравенств:");
        System.out.println(inequalitySystem);

        // -1 - найти минимум, +1 - найти максимум
        Equation targetFunction = new Equation(new ArrayList<>(Arrays.asList(+2.0, +2.0)), +1);
        System.out.println(targetFunction.toStringForFunction());

        EquationSystem equationSystem = inequalitySystem.toEquationSystem();
        System.out.println("Она же, но в уравнениях:");
        System.out.println(equationSystem);

        GomoryTable gomoryTable = new GomoryTable(equationSystem, targetFunction);
        System.out.println("Перевели в симплекс-таблицу:");
        System.out.println(gomoryTable);

        // Добиваемся хоть какого-нибудь оптимального решения
        getOptimal(gomoryTable);

        // Приводим решение к целочисленному
        while (!isIntegerSolution(gomoryTable, targetFunction.getNumberOfVariables())) {
            System.out.println("Нашли оптимальное нецелочисленное решение.");
            //ищем максимальную дробную часть
            String maxFractionalPartRow = gomoryTable.maxFractionalPartRow(targetFunction.getNumberOfVariables());
            double maxFractionalPart = gomoryTable.fractionalPart(gomoryTable.get("b", maxFractionalPartRow));
            System.out.println("Максимальная дробная часть у: " + maxFractionalPartRow);
            System.out.println("И она равна: " + maxFractionalPart);
            System.out.println();
            System.out.println("Для " + maxFractionalPartRow + " введём дополнительное ограничение");
            //новое ограничение для строки с максимальной дробной частью
            gomoryTable.createNewRestriction(maxFractionalPartRow);
            System.out.println("Теперь таблица имеет вид:");
            System.out.println(gomoryTable);
            System.out.println();

            System.out.println("Сделаем решение допустимым:");
            getOptimal(gomoryTable);
            System.out.println();
        }

        System.out.println("Нашли оптимальное целочисленное решение.");
        System.out.println();
        System.out.println("Значения переменных:");
        for (int i = 1; i <= targetFunction.getNumberOfVariables(); ++i) {
            System.out.print("x" + i + " = ");
            if (gomoryTable.exist("b", "x" + i)) {
                System.out.println(Utils.compressDouble(gomoryTable.get("b", "x" + i)));
            } else {
                System.out.println(0);
            }
        }
        System.out.print("Итоговый ответ: ");
        if (targetFunction.getResult() == 1) {
            System.out.println(Utils.compressDouble(gomoryTable.get("b", "F")));
        } else {
            System.out.println(Utils.compressDouble(-gomoryTable.get("b", "F")));
        }
    }

    private static void getOptimal(GomoryTable gomoryTable) {
        //ищем минимальный свободный
        String minXRowForBColumn = gomoryTable.minB();
        int iteration = 0;
        //пока среди свободных есть отрицательные - то это наша разрешающая строка
        while (gomoryTable.get("b", minXRowForBColumn) < 0.0) {
            System.out.println("Минимальный элемент среди свободных членов: " + gomoryTable.get("b", minXRowForBColumn));
            System.out.println("Среди свободных членов есть отрицательные. Нужно перейти к допустимому решению");
            //ищем разрешающий столбец
            String minXColumnForMinBRow = gomoryTable.minInRow(minXRowForBColumn);
            if (gomoryTable.get(minXColumnForMinBRow, minXRowForBColumn) >= 0.0) {
                System.out.println("Задачу решить нельзя");
                System.exit(0);
            }
            System.out.println("Ведущий столбец: " + minXColumnForMinBRow);
            System.out.println("Ведущая строка: " + minXRowForBColumn);
            System.out.println();

            ++iteration;
            System.out.println("Пересчитываем таблицу. Итерация " + iteration);
            gomoryTable.recalc(minXColumnForMinBRow, minXRowForBColumn);
            System.out.println(gomoryTable);

            // Обновляем минимум в b
            minXRowForBColumn = gomoryTable.minB();
        }

        // Удостоверимся, что нет -0.0
        gomoryTable.checkZero();

        // У целевой функции не должно быть отрицательных элементов
        iteration = 0;
        System.out.println(gomoryTable.get(gomoryTable.minInRow("F"), "F"));
        while (gomoryTable.get(gomoryTable.minInRow("F"), "F") < 0.0) {
            System.out.println("У целевой функции есть отрицательные элементы. Избавимся. Итерация " + iteration);

            // Разрешающий столбец - тот где отрицательный элемент
            String columnWithMinF = gomoryTable.minInRow("F");
            String rowPair = null;
            Double minDiv = null;

            // Вычисляем разрешающую строку - ту где b строки / элемент столбца будет минимальным
            for (String rowName : gomoryTable.getRowNames()) {
                if (gomoryTable.get(columnWithMinF, rowName) > 0.0) {
                    if (rowPair == null) {
                        rowPair = rowName;
                        minDiv = gomoryTable.get("b", rowName) / gomoryTable.get(columnWithMinF, rowName);
                    } else {
                        if (gomoryTable.get("b", rowName) / gomoryTable.get(columnWithMinF, rowName) < minDiv) {
                            rowPair = rowName;
                            minDiv = gomoryTable.get("b", rowName) / gomoryTable.get(columnWithMinF, rowName);
                        }
                    }
                }
            }
            if (rowPair == null) {
                System.out.println("Посчитать не получится");
                System.exit(0);
            }

            System.out.println("Разрешающий столбец: " + columnWithMinF);
            System.out.println("Разрешающая строка: " + rowPair);

            gomoryTable.recalc(columnWithMinF, rowPair);
            System.out.println("\n" + gomoryTable);
        }
    }

    private static boolean isIntegerSolution(GomoryTable gomoryTable, int numberOfVariables) {
        for (int i = 1; i <= numberOfVariables; ++i) {
            if (gomoryTable.exist("b", "x" + i)) {
                double value = gomoryTable.get("b", "x" + i);
                int valueInteger = (int) value;
                if (Math.abs(value - valueInteger) >= 0.0001) {
                    return false;
                }
            }
        }
        return true;
    }
}