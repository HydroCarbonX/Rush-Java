package io.hydrocarbon.rush.java.json;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        // 输入后序遍历和中序遍历
        String postOrder = in.next();
        String inOrder = in.next();
        TreeNode root = build(postOrder, inOrder);

        Queue<TreeNode> q = new LinkedList<>();
        q.offer(root);

        StringBuilder rs = new StringBuilder();
        while (!q.isEmpty()) {
            TreeNode poll = q.poll();
            rs.append(poll.val);

            if (poll.left != null) q.offer(poll.left);
            if (poll.right != null) q.offer(poll.right);
        }
        System.out.println(rs);
    }

    public static TreeNode build(String postOrder, String inOrder) {
        int len = postOrder.length();
        if (len == 0) {
            return null;
        } else if (len == 1) {
            return new TreeNode(postOrder.charAt(0));
        }

        // 根节点值
        char rootVal = postOrder.charAt(len - 1);
        TreeNode root = new TreeNode(rootVal);

        // 根节点在中序遍历中的索引位置, 此位置用于拆分左右子树
        int pos = inOrder.indexOf(rootVal);
        String leftPostOrder = postOrder.substring(0, pos);
        String rightPostOrder = postOrder.substring(pos, len - 1);

        String leftInOrder = inOrder.substring(0, pos);
        String rightInOrder = inOrder.substring(pos + 1);

        // 构造左右子树
        root.left = build(leftPostOrder, leftInOrder);
        root.right = build(rightPostOrder, rightInOrder);
        return root;
    }
}

class TreeNode {
    public char val;
    public TreeNode left;
    public TreeNode right;

    public TreeNode(char val) {
        this.val = val;
    }
}
