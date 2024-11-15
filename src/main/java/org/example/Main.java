package org.example;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import Jama.Matrix;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Main {

    private static Workbook loadWorkbook(FileInputStream file, String filename) throws IOException {
        var extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "xls":
                return new HSSFWorkbook(file);
            case "xlsx":
                return new XSSFWorkbook(file);
            default:
                throw new RemoteException("??? " + extension);
        }
    }

    public static double logLoss(ArrayList<Integer> isClass, ArrayList<Double> predictions) {
        double loss = 0;
        for (int i = 0; i < predictions.toArray().length; i++) {
            double c = isClass.get(i);
            double p = predictions.get(i) != 0 ? predictions.get(i) : Double.MIN_VALUE;
            loss += -c * Math.log(p) - (1 - c) * Math.log(1 - p);
        }

        return loss / predictions.toArray().length;
    }

    public static double sigmoid(double z) {
        return 1.0 / (1.0 + Math.exp(-z));
    }

    public static MyPoint2D toPolarPoint(MyPoint2D p) {
        double r = Math.pow(p.getX() * p.getX() + p.getY() * p.getY(), 0.5);
        double fi = Math.atan2(p.getY(), p.getX());

        return new MyPoint2D(r * Math.cos(3 * fi + Math.PI),
                r * Math.sin(3 * fi + Math.PI));
    }

    public static double predict(Matrix weights, double bias, MyPoint2D point) {
        Matrix z = weights.times(point.returnMatrix());
        return sigmoid(z.get(0, 0) + bias);
    }

    public static void main(String[] args) {
        LogisticRegression(new MyPoint2D(8, 8));
        LogisticRegression(new MyPoint2D(37, 1));
    }

    public static void LogisticRegression(MyPoint2D val) {
        ArrayList<MyPoint2D> point2DS = new ArrayList<>();
        ArrayList<Integer> isClass = new ArrayList<>();

        try {
            String pathname = "src\\main\\resources\\dataForLogisticRegression.xlsx";

            FileInputStream file = new FileInputStream(pathname);

            Workbook workbook = loadWorkbook(file, pathname);
            Sheet worksheet = null;

            if (workbook instanceof HSSFWorkbook) {
                HSSFWorkbook ssfWorkbook = (HSSFWorkbook) workbook;
                worksheet = ssfWorkbook.getSheetAt(0);
            } else if (workbook instanceof XSSFWorkbook) {
                XSSFWorkbook ssfWorkbook = (XSSFWorkbook) workbook;
                worksheet = ssfWorkbook.getSheetAt(0);
            }

            if (worksheet != null) {
                Iterator<Row> rowIterator = worksheet.iterator();
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();

                    Cell c = row.getCell(0);
                    Cell x = row.getCell(1);
                    Cell y = row.getCell(2);

                    MyPoint2D point = new MyPoint2D(x.getNumericCellValue(), y.getNumericCellValue());
                    point2DS.add(point);
                    isClass.add((int) c.getNumericCellValue());
                }
            }
            file.close();

            ArrayList<MyPoint2D> polarPoints = new ArrayList<>();
            for (MyPoint2D p : point2DS) {
                polarPoints.add(toPolarPoint(p));
            }

/*            for (MyPoint2D p : polarPoints) {
                System.out.println("( " + p.getX() + "; " + p.getY() + " )");
            }*/

            int len = polarPoints.toArray().length;
            Random rand = new Random();
            Matrix weights = new Matrix(1, 2, 0);
            for (int i = 0; i < 2; i++) {
                weights.set(0, i, rand.nextGaussian() * 0.001);  // Нормальное распределение
            }
            double bias = rand.nextGaussian() * 0.001;

            int reportEvery = 70;
            double learningRate = 0.001; // Шаг градиентного спуска
            ArrayList<Double> predictions = new ArrayList<>();

            System.out.println("----------------------------------------------------------------");
            int epochs = 210;
            for (int epoch = 0; epoch <= epochs; epoch++) {
                predictions.clear();
                Matrix dw = new Matrix(1, 2, 0);
                double db = 0;

                for (int i = 0; i < len; i++) {
                    predictions.add(predict(weights, bias, polarPoints.get(i)));
                    Matrix difference = new Matrix(1, 1, predictions.get(i) - isClass.get(i));
                    dw = dw.plus(difference.times(polarPoints.get(i).returnMatrix().transpose()));
                    db += predictions.get(i) - isClass.get(i);
                }

                dw = dw.times(1.0 / len);
                db /= len;

                //Вычисление градиента
                weights = weights.minus(dw.times(learningRate));
                bias -= learningRate * db;

                if (epoch % reportEvery == 0) {
                    double loss = logLoss(isClass, predictions);
                    System.out.println("Epoch: " + epoch + ", Log Loss: " + loss);
                }
            }

            System.out.println("Weights: " + Arrays.toString(weights.getArray()[0]));
            System.out.println("Bias: " + bias);

            System.out.println(">");
            System.out.println("Point: " + "[ " + val.getX() + ", " + val.getY() + " ]");
            double probability = predict(weights, bias, toPolarPoint(val));
            System.out.println("Probability: " + probability);
            System.out.print("Class: ");
            System.out.println(probability >= 0.5 ? 1 : 0);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}