package person.ziyu.level;

public class Main {

    public static void main(String[] args) {
        welcome();
        testSample();
    }

    private static void testSample() {
        Graph graph1 = GraphUtils.readDotfile("data/sample.dot");
        graph1.reverse();
        ResultSet resultSet1 = GraphUtils.level(graph1);
        resultSet1.toExcel("data/sample.xlsx");
    }

    private static void welcome() {
        String welcome = "Welcome to use person.ziyu.level!\nIf you have any question, Please contact me at ziyu.fu@outlook.com";
        System.out.println(welcome);
    }
}
