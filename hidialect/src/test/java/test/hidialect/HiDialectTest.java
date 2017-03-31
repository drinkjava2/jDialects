package test.hidialect;

import static com.github.drinkjava2.hidialect.HiDialect.Oracle10g;

/**
 * This is unit test for HiDialect
 * 
 * @author Yong Z.
 *
 */
public class HiDialectTest {
	public static void main(String[] args) {
		System.out.println(Oracle10g.pagin("select * from a order by a.id"));
	}
}
