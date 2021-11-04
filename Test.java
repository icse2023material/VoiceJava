public class SwitchTest {

    public static void main(string[] args) {
        int option = 2;
        switch(option) {
            case 1:
                System.out.println("Selected1");
                break;
            case 2:
            case 3:
                System.out.println("NotSelected");
                break;
        }
    }
}
