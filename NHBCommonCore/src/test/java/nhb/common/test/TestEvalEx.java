package nhb.common.test;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import com.udojava.evalex.Expression;

public class TestEvalEx {

	public static void main(String[] args) {
		BigDecimal result = null;
		Expression expression = new Expression("(1000000.4  + -4.1)/2");
		result = expression.eval();
		System.out.println(new DecimalFormat("###,###.##").format(result.doubleValue()));
	}
}
