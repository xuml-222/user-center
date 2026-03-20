package xuml.study.com.common.utils;

import xuml.study.com.model.dto.StudyDto;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Arrays;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class StrUtils {
    public static String longToStr(Long param) {
        return param == null ? "" : param.toString();
    }

    public static void main(String[] args) {
        /**
         * string stringBuffer stringBuilder
         */
//        stuStr();
        /**
         * set treeSte hashSet hashTree
         */
//        stuSetTree();

        te();
    }

    private static void te() {
        StudyDto dto = new StudyDto();
        dto.setName("xces");
        dto.setStudent("12");
        System.out.println(dto.getName());

        StudyDto dto1 = dto;

        dto1.setName("12111");

        System.out.println(dto.getName() + dto1.getName());


        StringBuilder str = new StringBuilder("121nn");
        StringBuilder str2 = str;
        str2 = new StringBuilder("ces");
        System.out.println(str2 + "====" + str);

//        study1();
//        st();
        stu();
    }
    public static void stu() {
       String str = new StringBuilder("ja").append("va").toString();
        System.out.println(str == str.intern());
        String str2 = new StringBuilder("ja1").append("va").toString();
        System.out.println(str2 == str2.intern());
        /**
         * string stringBuffer stringBuilder
         */
        stuStr();
        /**
         * set treeSte hashSet hashTree
         */
        stuSetTree();
    }

    private static void stuSetTree() {
        Set<String> set1 = new HashSet<>();
        Set<String> set2 = new TreeSet<>();
        set1.add(null);
        set2.add(null);

        TreeSet<String> tSet = new TreeSet<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return 0;
            }
        });
    }


    public static void stuStr() {
        List<String> list = Arrays.asList("ja", "va", " ", "1.8");
        AtomicReference<String> a = new AtomicReference<>("");
        list.forEach(item -> {
            a.set(a + item);
        });

        StringBuilder b = new StringBuilder();
        for (String str : list) {
            b.append(str);
        }

        /**
         * 线程安全 synchronized关键字修饰
         */
        StringBuffer c = new StringBuffer();

        list.forEach(c::append);
        System.out.println(a);
        System.out.println("===========================");
        System.out.println(b);
        System.out.println("===========================");
        System.out.println(c);
    }
}
