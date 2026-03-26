package xuml.study.com.common.utils;


public class Math<E> {
    public static void main(String[] args) {
        LinkList<Integer> linkList = new LinkList<>();
        linkList.add(1);
        linkList.add(2);
        linkList.add(3);
        linkList.add(4);
        linkList.add(5);
        Node<Integer> oldNode = linkList.list;
        linkList.remove(0);
        NodeReturn<Integer> nodeReturn = new NodeReturn<>();
        Node<Integer> node = nodeReturn.removeReturnNode(oldNode, 2);
        while (node.next != null) {
            System.out.println(node.data);
            node = node.next;
        }
        System.out.println(node.data);
    }


    public static class NodeReturn<E> {
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

    }

    //链表
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

        public void remove(int index) {
            if (list != null) {
                //快慢指针
                Node<E> temp = new Node<>(list, null);
                Node<E> fast = temp;
                Node<E> slow = temp;
                //先走N步
                for (int i = 0; i < index; i++) {
                    if (fast.next != null) {
                        fast = fast.next;
                    } else {
                        return;
                    }
                }
                //再一起走 slow 走到要删除的节点的前一个节点 fast走到最后一个节点
                while (fast.next != null) {
                    fast = fast.next;
                    slow = slow.next;
                }
                if (slow.next != null) {
                    slow.next = slow.next.next;
                    size--;
                }
                list = temp.next;
            }

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
