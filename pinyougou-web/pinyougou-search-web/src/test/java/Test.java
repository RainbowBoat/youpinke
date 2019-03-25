import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        List<List<Integer>> bigList = new ArrayList<>();
        List<Integer> smallList1 = new ArrayList<>();
        List<Integer> smallList2 = new ArrayList<>();
        smallList1.add(1);
        smallList1.add(2);
        smallList1.add(3);
        smallList2.add(1);
        smallList2.add(2);
        smallList2.add(3);
        bigList.add(smallList1);
        bigList.add(smallList2);

        System.out.println(bigList);

        bigList.get(0).remove(1);

        System.out.println(bigList);
    }
}
