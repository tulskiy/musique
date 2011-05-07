package org.jaudiotagger;

/**
 * User: paul
 * Date: 09-Jun-2009
 */
public class Test {
    static void writeIt() {
        System.out.println("hi");
    }
}

class Test2 extends Test {
    public static void main(String[] args) {
        Test2 test2 = new Test2();
        Test2.writeIt();
    }
}


