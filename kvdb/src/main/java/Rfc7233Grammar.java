import java.nio.ByteBuffer;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Rfc7233Grammar {
    private static final Pattern NOSPACES = Pattern.compile("\\b+");
    private static final Pattern COMPILE = Pattern.compile("^(\\w+=)?\\d*-\\d*(,\\d*-\\d*)*$");
    static ThreadLocal<ByteBuffer> YYMARKER = new ThreadLocal<>();
    static ThreadLocal<Integer> yyaccept = new ThreadLocal<>();
    static ThreadLocal<Integer> yych = new ThreadLocal<>() ;
    static ThreadLocal<Integer> YYSTATE = ThreadLocal.withInitial(() -> -1);

    public String scan(ByteBuffer p) {

        goto0:while(true)
        switch ((int) YYSTATE.get()) {
            case 0:
            {
                yych.set((int) 0xff & p.get(p.position()));
                if (yych.get() <= '=') if (yych.get() > '.') {
                    if (yych.get() > '^') {
                        if (yych.get() == '`') {yy2(p);continue goto0;};
                        if (yych.get() <= 'z') {yy10(p);continue goto0;};
                    } else {
                        if (yych.get() <= '@') {yy2(p);continue goto0;};
                        if (yych.get() <= 'Z') {yy10(p);continue goto0;};
                    }
                } else if (yych.get() == '-') {yy10(p);continue goto0;};
                else {
                    if (yych.get() <= '/') {yy8(p);continue goto0;};
                    if (yych.get() <= '9') {yy3(p);continue goto0;};
                    if (yych.get() >= '=') {yy6(p);continue goto0;};
                }
            }
            yy2:
                p = YYMARKER.get();
            yyaccept.set(0);
            if (yyaccept.get() <= 1) {
                if (yyaccept.get() <= 0) {
                    {yy5(p);continue goto0;};
                } else {
                    {yy7(p);continue goto0;};
                }
            } else {
                if (yyaccept.get() <= 2) {
                    {yy12(p);continue goto0;};
                } else {
                {yy22(p);continue goto0;};
                }
            }
            yy3:
                yyaccept.set(0);
            YYMARKER.set(skip1(p));
            YYSTATE.set(1);
            case 1:
                yyFillLabel1:
                yych.set((int) 0xff & p.get(p.position()));
                if (yych.get() <= '<') {
                    if (yych.get() <= '-') {
                        if (yych.get() <= '+') {yy5(p);continue goto0;};
                        if (yych.get() <= ',') {yy19(p);continue goto0;};
                        {yy10(p);continue goto0;};
                    } else {
                        if (yych.get() <= '.') {yy5(p);continue goto0;};
                        if (yych.get() <= '/') {yy18(p);continue goto0;};
                        if (yych.get() <= '9') {yy3(p);continue goto0;};
                    }
                } else {
                    if (yych.get() <= '^') {
                        if (yych.get() <= '=') {yy14(p);continue goto0;};
                        if (yych.get() <= '@') {yy5(p);continue goto0;};
                        if (yych.get() <= 'Z') {yy10(p);continue goto0;};
                    } else {
                        if (yych.get() == '`') {yy5(p);continue goto0;};
                        if (yych.get() <= 'z') {yy10(p);continue goto0;};
                    }
                }
                yy5:
                {
                    return String.valueOf(UTF_8.decode(p));
                }
                yy6:
                yyaccept.set(1);
                YYMARKER.set(p.duplicate());
                yych.set(p.get()&0xff);
                if (yych.get() == ',') {yy17(p);continue goto0;};
                if (yych.get() <= '.') {yy7(p);continue goto0;};
                if (yych.get() <= '9') {yy17(p);continue goto0;};
                yy7:
                {
                    return "onward to";
                }
                yy8:
                skip1(p);
            {
                return "of";
            }
            yy10:
            yyaccept.set(2);
            YYMARKER.set(skip1(p));
            YYSTATE.set(2);
            case 2:
                yyFillLabel2:
                yych.set((int) 0xff & p.get(p.position()));
                if (yych.get() <= '=') {
                    if (yych.get() <= '/') {
                        if (yych.get() == '-') {yy10(p);continue goto0;};
                    } else {
                        if (yych.get() <= '9') {yy10(p);continue goto0;};
                        if (yych.get() >= '=') {yy13(p);continue goto0;};
                    }
                } else {
                    if (yych.get() <= '^') {
                        if (yych.get() <= '@') {yy12(p);continue goto0;};
                        if (yych.get() <= 'Z') {yy10(p);continue goto0;};
                    } else {
                        if (yych.get() == '`') {yy12(p);continue goto0;};
                        if (yych.get() <= 'z') {yy10(p);continue goto0;};
                    }
                }
                yy12:
                {
                    return UTF_8.decode(p).toString();
                }
                yy13:
                yych.set((int) 0xff & (p).get());
                if (yych.get() <= '/') {yy2(p);continue goto0;};
                if (yych.get() <= '9') {yy14(p);continue goto0;};
                if (yych.get() == '=') {yy16(p);continue goto0;};
                {yy2(p);continue goto0;};
            yy14:
            skip1(p);
            YYSTATE.set(3);

            case 3:
                yyFillLabel3:
                yych.set((int) 0xff & p.get(p.position()));
                if (yych.get() <= '/') {
                    if (yych.get() == ',') {yy19(p);continue goto0;};
                    if (yych.get() <= '.') {yy2(p);continue goto0;};
                    {yy18(p);continue goto0;};
                } else {
                    if (yych.get() <= '9') {yy14(p);continue goto0;};
                    if (yych.get() != '=') {yy2(p);continue goto0;};
                }
                yy16:
                skip1(p);
                YYSTATE.set(4);
            case 4:
                yyFillLabel4:
                yych.set((int) 0xff & p.get(p.position()));
                yy17:
                if (yych.get() <= '.') {
                    if (yych.get() == ',') {yy19(p);continue goto0;};
                    {yy2(p);continue goto0;};
                } else {
                    if (yych.get() <= '/') {yy18(p);continue goto0;};
                    if (yych.get() <= '9') {yy16(p);continue goto0;};
                    {yy2(p);continue goto0;};
                }
                yy18:
                yych.set((int) 0xff & (p).get());
                if (yych.get() <= '/') {yy2(p);continue goto0;};
                if (yych.get() <= '9') {yy30(p);continue goto0;};
                if (yych.get() == '=') {yy29(p);continue goto0;};
                {yy2(p);continue goto0;};
            yy19:
            skip1(p);
            YYSTATE.set(5);
            case 5:
                yyFillLabel5:
                yych.set((int) 0xff & p.get(p.position()));
                if (yych.get() <= '/') {yy2(p);continue goto0;};
                if (yych.get() <= '9') {yy20(p);continue goto0;};
                if (yych.get() == '=') {yy23(p);continue goto0;};
                {yy2(p);continue goto0;};
            yy20:
            yyaccept.set(3);
            YYMARKER.set(skip1(p));
            YYSTATE.set(6);
            case 6:
                yyFillLabel6:
                yych.set((int) 0xff & p.get(p.position()));
                if (yych.get() <= '/') {
                    if (yych.get() == ',') {yy19(p);continue goto0;};
                    if (yych.get() >= '/') {yy25(p);continue goto0;};
                } else {
                    if (yych.get() <= '9') {yy20(p);continue goto0;};
                    if (yych.get() == '=') {yy23(p);continue goto0;};
                }
                yy22:
                {
                    return "found legal range where";
                }
                yy23:
                yyaccept.set(3);
                YYMARKER.set(skip1(p));
                YYSTATE.set(7);
            case 7:
                yyFillLabel7:
                yych.set((int) 0xff & p.get(p.position()));
                if (yych.get() <= '.') {
                    if (yych.get() == ',') {yy19(p);continue goto0;};
                    {yy22(p);continue goto0;};
                } else {
                    if (yych.get() <= '/') {yy25(p);continue goto0;};
                    if (yych.get() <= '9') {yy23(p);continue goto0;};
                    {yy22(p);continue goto0;};
                }
                yy25:
                skip1(p);
                YYSTATE.set(8);
            case 8:
                yyFillLabel8:
                yych.set((int) 0xff & p.get(p.position()));
                if (yych.get() <= '/') {yy2(p);continue goto0;};
                if (yych.get() <= '9') {yy27(p);continue goto0;};
                if (yych.get() != '=') {yy2(p);continue goto0;};
                skip1(p);
                YYSTATE.set(9);
            case 9:
                yyFillLabel9:
                yych.set((int) 0xff & p.get(p.position()));
                if (yych.get() <= '/') {yy2(p);continue goto0;};
                if (yych.get() >= ':') {yy2(p);continue goto0;};
                yy27:
                yyaccept.set(3);
                YYMARKER.set(skip1(p));
                YYSTATE.set(10);
            case 10:
                yyFillLabel10:
                yych.set((int) 0xff & p.get(p.position()));
                if (yych.get() == ',') {yy19(p);continue goto0;};
                if (yych.get() <= '/') {yy22(p);continue goto0;};
                if (yych.get() <= '9') {yy27(p);continue goto0;};
                {yy22(p);continue goto0;};
            yy29:
            yych.set((int) 0xff & p.get());
            if (yych.get() <= '/') {yy2(p);continue goto0;};
            if (yych.get() >= ':') {yy2(p);continue goto0;};
            yy30:
            skip1(p);
            YYSTATE.set(11);
            case 11:
                yyFillLabel11:
                yych.set((int) 0xff & p.get(p.position()));
                if (yych.get() == ',') {yy19(p);continue goto0;};
                if (yych.get() <= '/') {yy2(p);continue goto0;};
                if (yych.get() <= '9') {yy30(p);continue goto0;};
                {yy2(p);continue goto0;};
        } 
    }

    private ByteBuffer skip1(ByteBuffer p) {
        return (ByteBuffer) p.position(p.position() + 1);
    }

}

