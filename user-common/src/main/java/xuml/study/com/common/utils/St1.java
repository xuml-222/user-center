package xuml.study.com.common.utils;

import java.util.ArrayList;
import java.util.List;

public class St1 {
    //
    public static void main(String[] args) {
        vlan(new String[]{"1", "2", "3", "15-19", "11-12", "21", "22", "5-9"}, "11");
    }

    public static void vlan(String[] vlan, String input) {
        int index = 0;
        //排序
        for (int i = 0; i < vlan.length; i++) {
            String amin = vlan[i];
            if (vlan[i].contains("-")) {
                amin = vlan[i].split("-")[0];
            }
            for (int j = 0; j < vlan.length; j++) {
                String bMin = vlan[j];
                if (vlan[j].contains("-")) {
                    bMin = vlan[j].split("-")[0];
                }
                if (Integer.parseInt(amin) < Integer.parseInt(bMin)) {
                    String temp = vlan[i];
                    vlan[i] = vlan[j];
                    vlan[j] = temp;
                }
            }
        }
        //合并
        List<String> res = new ArrayList<String>();
        List<String> temp = new ArrayList<String>();
        for (int i = 0; i < vlan.length; i++) {
            String min = vlan[i];
            String max = null;
            if (i > 0 && !temp.isEmpty()) {
                if (Integer.parseInt(min.split("-")[0]) - Integer.parseInt(temp.get(temp.size() - 1)) != 1) {
                    if (temp.size() > 1) {
                        res.add(temp.get(0) + "-" + temp.get(temp.size() - 1));
                    } else {
                        res.add(temp.get(0));
                    }
                    temp.clear();
                }
            }
            if (vlan[i].contains("-")) {
                min = vlan[i].split("-")[0];
                max = vlan[i].split("-")[1];
                temp.add(min);
                temp.add(max);
            } else {
                temp.add(vlan[i]);
            }
        }
        if (!temp.isEmpty()) {
            if (temp.size() > 1) {
                res.add(temp.get(0) + "-" + temp.get(temp.size() - 1));
            } else {
                res.add(temp.get(0));
            }
        }
        //取数
        temp.clear();
        for (int i = 0; i < res.size(); i++) {
            String amin = res.get(i);
            String aMax = "";
            if (res.get(i).contains("-")) {
                amin = res.get(i).split("-")[0];
                aMax = res.get(i).split("-")[1];
            }
            if (Integer.parseInt(amin) == Integer.parseInt(input)) {
                if (Integer.parseInt(aMax) == (Integer.parseInt(input) +1)) {
                    temp.add(aMax);
                } else {
                    temp.add(i, Integer.parseInt(input) + 1 + "-" + Integer.parseInt(aMax));
                }
                continue;
            } else if (aMax != null && !aMax.isEmpty() && Integer.parseInt(aMax) == Integer.parseInt(input)) {
                if (Integer.parseInt(amin) == (Integer.parseInt(input) - 1)) {
                    temp.add(amin);
                } else {
                    temp.add(i, Integer.parseInt(amin) + "-" + (Integer.parseInt(input) - 1));
                }
                continue;
            } else if (aMax != null && !aMax.isEmpty() & Integer.parseInt(amin) < Integer.parseInt(input) && Integer.parseInt(aMax) > Integer.parseInt(input)) {
                if (Integer.parseInt(amin) == (Integer.parseInt(input) - 1)) {
                    temp.add(amin);
                } else {
                    temp.add(amin + "-" + (Integer.parseInt(input) - 1));
                }
                if (Integer.parseInt(aMax) == (Integer.parseInt(input) + 1)) {
                    temp.add(aMax);
                } else {
                    temp.add((Integer.parseInt(input) + 1) + "-" + aMax);
                }

                continue;
            }
            temp.add(res.get(i));
        }
        temp.forEach(System.out::println);

    }
}
