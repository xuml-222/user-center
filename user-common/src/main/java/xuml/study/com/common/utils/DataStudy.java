package xuml.study.com.common.utils;

public class DataStudy {

    /**
     * 稀疏数组
     */
    public static void sparseArray() {
        //创建一个原始的二维数组
        //0-没有棋子 1-黑子 2-白子
        int[][] chessArr1 = new int[11][11];
        chessArr1[1][2] = 1;
        chessArr1[2][3] = 2;
        System.out.println("===== 原始二维数组 =====");
        for (int[] row : chessArr1) {
            for (int col : row) {
                System.out.printf("%d\t", col);
            }
            System.out.print("\n");
        }

        //将二维数组 转 稀疏数组
        //1.先遍历二维数组 得到非零的数据个数
        int sum = 0;
        for (int i = 0; i < chessArr1.length; i++) {
            for (int j = 0; j < chessArr1[i].length; j++) {
                if (chessArr1[i][j] != 0) {
                    sum++;
                }
            }
        }

        //2.创建对应的稀疏数组
        int[][] sparseArr = new int[sum + 1][3];
        //给稀疏数组赋值
        sparseArr[0][0] = 11;
        sparseArr[0][1] = 11;
        sparseArr[0][2] = sum;

        //变量二维数组
        int count = 0;
        for (int i = 0; i < chessArr1.length; i++) {
            for (int j = 0; j < chessArr1[i].length; j++) {
                if (chessArr1[i][j] != 0) {
                    count++;
                    sparseArr[count][0] =i;
                    sparseArr[count][1] = j;
                    sparseArr[count][2] =chessArr1[i][j];
                }
            }
        }

        System.out.println();
        System.out.println("====== 得到稀疏数组为 ======");
        for (int[] ints : sparseArr) {
            System.out.printf("%d\t", ints[0]);
            System.out.printf("%d\t", ints[1]);
            System.out.printf("%d\t", ints[2]);
            System.out.println();
        }

        //稀疏数组 -> 二维数组
        int[][] chessArr2 = new int[sparseArr[0][0]][sparseArr[0][1]];
        for (int i = 1; i< sparseArr.length;i++){
            chessArr2[sparseArr[i][0]][sparseArr[i][1]] = sparseArr[i][2];
        }
        System.out.println();
        System.out.println("====== 恢复后稀疏数组为 ======");
        for (int[] row : chessArr2) {
            for (int col : row) {
                System.out.printf("%d\t", col);
            }
            System.out.print("\n");
        }
    }

    public static void main(String[] args) {
        sparseArray();
    }

}
