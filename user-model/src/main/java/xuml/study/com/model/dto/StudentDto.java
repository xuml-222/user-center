package xuml.study.com.model.dto;

import java.util.Comparator;
import java.util.TreeSet;

public class StudentDto /*implements Comparable<StudentDto>*/ {

    private String name;
    private int age;

    public StudentDto(String name, int age) {
        this.age = age;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
  /*  @Override
    public int compareTo(StudentDto o) {
        //按照年龄排序

        int result = this.getAge() - o.getAge();
        return result = result == 0 ? this.getName().compareTo(o.getName()) : result;
    }*/

    public static void main(String[] args) {
        test();
    }

    private static void test() {
        //创建集合对象 实体类继承 Comparable  new TreeSet<> 传入 Comparator
        TreeSet<StudentDto> ts = new TreeSet<>(new Comparator<StudentDto>() {
            @Override
            public int compare(StudentDto o1, StudentDto o2) {
                int result = o1.getAge() - o2.getAge();
                return result = result == 0 ? o1.getName().compareTo(o2.getName()) : result;
            }
        });
        //创建学生对象
        StudentDto s1 = new StudentDto("zhangsan", 28);
        StudentDto s2 = new StudentDto("lisi", 27);
        StudentDto s3 = new StudentDto("wangwu", 29);
        StudentDto s4 = new StudentDto("zhaoliu", 28);
        StudentDto s5 = new StudentDto("qianqi", 30);
        //把学生添加到集合
        ts.add(s1);
        ts.add(s2);
        ts.add(s3);
        ts.add(s4);
        ts.add(s5);
        //遍历集合
        for (StudentDto student : ts) {
            System.out.println(student.toString());
        }
    }
}
