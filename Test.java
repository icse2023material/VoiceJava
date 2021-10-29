public class Puppy {

    int puppyAge;

    public void setAge(int age) {
        puppyAge = age;
    }

    public int getAge() {
        return puppyAge;
    }

    public static void main(String[] args) {
        Puppy myPuppy = new Puppy("tommy");
    }
}
