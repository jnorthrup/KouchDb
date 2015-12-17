import sun.nio.cs.UTF_8;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Created by jim on 12/15/2015.
 */
public class Rfc7233Grammar implements UnaryOperator<ByteBuffer> {

    private static final Pattern NOSPACES = Pattern.compile("\\b+");
    private static final Pattern COMPILE = Pattern.compile("^(\\w+=)?\\d*-\\d*(,\\d*-\\d*)*$");

    
    ThreadLocal<Integer> YYSTATE=ThreadLocal.withInitial(() -> -1);
    final ThreadLocal<ByteBuffer> YYMARKER = new ThreadLocal<>();
    String scan(ByteBuffer p)
    {


        int yych;
        switch (YYSTATE.get()) {
            case 0:
                yyFillLabel0:
                yych = (int)0xff&p.get(p.position());
                if (yych <= '=') {
                    if (yych <= '.') {
                        if (yych == '-') return yy10(p);
                    } else {
                        if (yych <= '/') return yy8(p);
                        if (yych <= '9') return yy3(p);
                        if (yych >= '=') return yy6(p);
                    }
                } else {
                    if (yych <= '^') {
                        if (yych <= '@') return yy2(p);
                        if (yych <= 'Z') return yy10(p);
                    } else {
                        if (yych == '`') return yy2(p);
                        if (yych <= 'z') return yy10(p);
                    }
                }
                yy2:
                p=YYMARKER.get();
                int yyaccept;
                if (yyaccept <= 1) {
                    if (yyaccept <= 0) {
                        return yy5(p);
                    } else {
                        return yy7(p);
                    }
                } else {
                    if (yyaccept <= 2) {
                        return yy12(p);
                    } else {
                        return yy22(p);
                    }
                }
                yy3:
                yyaccept = 0;
                YYMARKER.set(skip1(p));
                YYSTATE.set(1);
            case 1:
                yyFillLabel1:
                yych = (int)0xff&p.get(p.position());
                if (yych <= '<') {
                    if (yych <= '-') {
                        if (yych <= '+') return yy5(p);
                        if (yych <= ',') return yy19(p);
                        return yy10(p);
                    } else {
                        if (yych <= '.') return yy5(p);
                        if (yych <= '/') return yy18(p);
                        if (yych <= '9') return yy3(p);
                    }
                } else {
                    if (yych <= '^') {
                        if (yych <= '=') return yy14(p);
                        if (yych <= '@') return yy5(p);
                        if (yych <= 'Z') return yy10(p);
                    } else {
                        if (yych == '`') return yy5(p);
                        if (yych <= 'z') return yy10(p);
                    }
                }
                yy5:
                {return String.valueOf(StandardCharsets.UTF_8.decode(p));}
                yy6:
                yyaccept = 1;
                YYMARKER .set(p.duplicate());
                yych = p.get();
                if (yych == ',') return yy17(p);
                if (yych <= '.') return yy7(p);
                if (yych <= '9') return yy17(p);
                yy7:
                {return "onward to";}
                yy8:
                skip1(p);
            {return "of";}
            yy10:
            yyaccept = 2;
            YYMARKER.set(skip1(p));
            YYSTATE.set(2);
            case 2:
                yyFillLabel2:
                yych = (int)0xff&p.get(p.position());
                if (yych <= '=') {
                    if (yych <= '/') {
                        if (yych == '-') return yy10(p);
                    } else {
                        if (yych <= '9') return yy10(p);
                        if (yych >= '=') return yy13(p);
                    }
                } else {
                    if (yych <= '^') {
                        if (yych <= '@') return yy12(p);
                        if (yych <= 'Z') return yy10(p);
                    } else {
                        if (yych == '`') return yy12(p);
                        if (yych <= 'z') return yy10(p);
                    }
                }
                yy12:
                {return p;}
                yy13:
                yych = (int)0xff&(++p;).get()
                if (yych <= '/') return yy2(p);
                if (yych <= '9') return yy14(p);
                if (yych == '=') return yy16(p);
                return yy2(p);
            yy14:
            skip1(p);
            YYSTATE.set(3);

            case 3:
                yyFillLabel3:
                yych = (int)0xff&p.get(p.position());
                if (yych <= '/') {
                    if (yych == ',') return yy19(p);
                    if (yych <= '.') return yy2(p);
                    return yy18(p);
                } else {
                    if (yych <= '9') return yy14(p);
                    if (yych != '=') return yy2(p);
                }
                yy16:
                skip1(p);
                YYSTATE.set(4);
            case 4:
                yyFillLabel4:
                yych = (int)0xff&p.get(p.position());
                yy17:
                if (yych <= '.') {
                    if (yych == ',') return yy19(p);
                    return yy2(p);
                } else {
                    if (yych <= '/') return yy18(p);
                    if (yych <= '9') return yy16(p);
                    return yy2(p);
                }
                yy18:
                yych = (int)0xff&(++p;).get()
                if (yych <= '/') return yy2(p);
                if (yych <= '9') return yy30(p);
                if (yych == '=') return yy29(p);
                return yy2(p);
            yy19:
            skip1(p);
            YYSTATE.set(5);
            case 5:
                yyFillLabel5:
                yych = (int)0xff&p.get(p.position());
                if (yych <= '/') return yy2(p);
                if (yych <= '9') return yy20(p);
                if (yych == '=') return yy23(p);
                return yy2(p);
            yy20:
            yyaccept = 3;
            YYMARKER.get().set(skip1(p));
            YYSTATE.set(6);
            case 6:
                yyFillLabel6:
                yych = (int)0xff&p.get(p.position());
                if (yych <= '/') {
                    if (yych == ',') return yy19(p);
                    if (yych >= '/') return yy25(p);
                } else {
                    if (yych <= '9') return yy20(p);
                    if (yych == '=') return yy23(p);
                }
                yy22:
                { return "found legal range where"}
                yy23:
                yyaccept = 3;
                YYMARKER.get().set(skip1(p));
                YYSTATE.set(7);
            case 7:
                yyFillLabel7:
                yych = (int)0xff&p.get(p.position());
                if (yych <= '.') {
                    if (yych == ',') return yy19(p);
                    return yy22(p);
                } else {
                    if (yych <= '/') return yy25(p);
                    if (yych <= '9') return yy23(p);
                    return yy22(p);
                }
                yy25:
                skip1(p);
                YYSTATE.set(8);
            case 8:
                yyFillLabel8:
                yych = (int)0xff&p.get(p.position());
                if (yych <= '/') return yy2(p);
                if (yych <= '9') return yy27(p);
                if (yych != '=') return yy2(p);
                skip1(p);
                YYSTATE.set(9);
            case 9:
                yyFillLabel9:
                yych = (int)0xff&p.get(p.position());
                if (yych <= '/') return yy2(p);
                if (yych >= ':') return yy2(p);
                yy27:
                yyaccept = 3;
                YYMARKER.get().set(skip1(p));
                YYSTATE.set(10);
            case 10:
                yyFillLabel10:
                yych = (int)0xff&p.get(p.position());
                if (yych == ',') return yy19(p);
                if (yych <= '/') return yy22(p);
                if (yych <= '9') return yy27(p);
                return yy22(p);
            yy29:
            yych = (int)0xff&p.get();
            if (yych <= '/') return yy2(p);
            if (yych >= ':') return yy2(p);
            yy30:
            skip1(p);
            YYSTATE.set(11);
            case 11:
                yyFillLabel11:
                yych = (int)0xff&p.get(p.position());
                if (yych == ',') return yy19(p);
                if (yych <= '/') return yy2(p);
                if (yych <= '9') return yy30(p);
                return yy2(p);
            default:
                YYSTATE.set(0);return scan(p);
        }

    }

    private ByteBuffer skip1(ByteBuffer p) {
           return  (ByteBuffer)p.position(p.position()+1);
    }


}