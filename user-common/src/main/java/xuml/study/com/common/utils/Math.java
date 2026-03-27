package xuml.study.com.common.utils;


import java.util.HashMap;
import java.util.Stack;

public class Math {
    public static void main(String[] args) {
        test1();

    }

    public static void test2() {
        Stack<Character> stack = new Stack<>();

    }

    //两数之和
    public int[] twoSum(int[] nums, int target) {
        HashMap<Integer, Integer> map = new HashMap<>();

        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i]; // 计算补数

            // 补数存在，返回结果
            if (map.containsKey(complement)) {
                return new int[]{map.get(complement), i};
            }

            // 不存在，存入 map
            map.put(nums[i], i);
        }
        return new int[0];
    }

    public boolean isValid(String s) {
        Stack<Character> stack = new Stack<>();

        for (char c : s.toCharArray()) {
            // 1. 左括号入栈
            if (c == '(' || c == '{' || c == '[') {
                stack.push(c);
            }
            // 2. 右括号匹配
            else {
                // 栈空说明没有左括号，直接无效
                if (stack.isEmpty()) return false;

                char top = stack.pop();
                // 判断是否匹配
                if (c == ')' && top != '(') return false;
                if (c == '}' && top != '{') return false;
                if (c == ']' && top != '[') return false;
            }
        }
        // 3. 最后栈必须为空才算有效
        return stack.isEmpty();
    }

    public boolean isValid2(String s) {
        Stack<Character> stack = new Stack<>();
        for (char c : s.toCharArray()) {
            if (c == '(') stack.push(')');
            else if (c == '{') stack.push('}');
            else if (c == '[') stack.push(']');
            else if (stack.isEmpty() || stack.pop() != c) return false;
        }
        return stack.isEmpty();
    }


    public static void test1() {
        LinkList<Integer> linkList = new LinkList<>();
        linkList.add(1);
        linkList.add(2);
        linkList.add(3);
        linkList.add(4);
        linkList.add(5);
        Node<Integer> oldNode = linkList.list;
        NodeReturn<Integer> nodeReturn = new NodeReturn<>();
        Node<Integer> node = nodeReturn.removeReturnNode(oldNode, 2);
        while (node.next != null) {
            System.out.println(node.data);
            node = node.next;
        }
        System.out.println(node.data);
    }


    public static class NodeReturn<E> {
        //1.	给定一个链表，使用一趟扫描实现，删除链表的倒数第 n 个节点，并且返回链表的头结点
        public Node<E> removeReturnNode(Node<E> node, Integer index) {
            if (node != null) {
                //快慢指针
                Node<E> temp = new Node<>(node, null);
                Node<E> fast = temp;
                Node<E> slow = temp;
                //先走N步
                for (int i = 0; i < index; i++) {
                    if (fast.next != null) {
                        fast = fast.next;
                    } else {
                        return node;
                    }
                }
                //再一起走 slow 走到要删除的节点的前一个节点 fast走到最后一个节点
                while (fast.next != null) {
                    fast = fast.next;
                    slow = slow.next;
                }
                if (slow.next != null) {
                    slow.next = slow.next.next;
                }
                return temp.next;
            } else {
                return null;
            }
        }

        public Node<E> distinct(Node<E> node) {
            if (node != null) {
                Node<E> temp = node;
                while (temp.next != null) {
                    if (temp.data.equals(temp.next.data)) {
                        temp.next = temp.next.next;
                    } else {
                        temp = temp.next;
                    }
                }
                return node;
            } else {
                return null;
            }
        }

    }

    public static class Node<E> {
        E data;
        Node<E> next;

        Node(Node<E> next, E data) {
            this.next = next;
            this.data = data;
        }
    }

    //链表
    public static class LinkList<E> {
        private Node<E> list;
        private int size;

        public E get(int index) {
            if (list != null) {
                Node<E> temp = list;
                for (int i = 0; i < index; i++) {
                    if (temp.next != null) {
                        temp = temp.next;
                    } else {
                        return null;
                    }
                }
                return temp.data;
            }
            return null;
        }

        //头插
        public void addFirst(E data) {
            this.list = new Node<>(this.list, data);
            size++;
        }

        //尾插
        public void addLast(E data) {
            if (list == null) {
                addFirst(data);
            } else {
                Node<E> temp = list;
                while (temp.next != null) {
                    temp = temp.next;
                }
                temp.next = new Node<>(null, data);
            }
        }

        public void add(E data) {
            addLast(data);
        }

        public void add(E data, int index) {
            if (list == null) {
                addFirst(data);
            } else {
                Node<E> temp = list;
                int count = 0;
                while (temp.next != null) {
                    if (count == index) {
                        temp.next = new Node<>(temp.next, data);
                        break;
                    }
                    count++;
                }
            }
        }
    }
}
