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

        switch ((int) YYSTATE.get()) {
            case 0:
            {
                yych.set((int) 0xff & p.get(p.position()));
                if (yych.get() <= '=') if (yych.get() > '.') {
                    if (yych.get() > '^') {
                        if (yych.get() == '`') return yy2(p);
                        if (yych.get() <= 'z') return yy10(p);
                    } else {
                        if (yych.get() <= '@') return yy2(p);
                        if (yych.get() <= 'Z') return yy10(p);
                    }
                } else if (yych.get() == '-') return yy10(p);
                else {
                    if (yych.get() <= '/') return yy8(p);
                    if (yych.get() <= '9') return yy3(p);
                    if (yych.get() >= '=') return yy6(p);
                }
            }
            yy2:
                p = YYMARKER.get();
            yyaccept.set(0);
            if (yyaccept.get() <= 1) {
                if (yyaccept.get() <= 0) {
                    return yy5(p);
                } else {
                    return yy7(p);
                }
            } else {
                if (yyaccept.get() <= 2) {
                    return yy12(p);
                } else {
                return yy22(p);
                }
            }
            yy3:
                yyaccept.set(0);
            YYMARKER.set(skip1(p));
            YYSTATE.set(1);return scan(p);
            case 1:
                yyFillLabel1:
                yych.set((int) 0xff & p.get(p.position()));
                if (yych.get() <= '<') {
                    if (yych.get() <= '-') {
                        if (yych.get() <= '+') return yy5(p);
                        if (yych.get() <= ',') return yy19(p);
                        return yy10(p);
                    } else {
                        if (yych.get() <= '.') return yy5(p);
                        if (yych.get() <= '/') return yy18(p);
                        if (yych.get() <= '9') return yy3(p);
                    }
                } else {
                    if (yych.get() <= '^') {
                        if (yych.get() <= '=') return yy14(p);
                        if (yych.get() <= '@') return yy5(p);
                        if (yych.get() <= 'Z') return yy10(p);
                    } else {
                        if (yych.get() == '`') return yy5(p);
                        if (yych.get() <= 'z') return yy10(p);
                    }
                }
                yy5:
                {
                    return String.valueOf(UTF_8.decode(p));
                }
                yy6:
                yyaccept.set(1);
                YYMARKER.set(p.duplicate());
                yych.set(p.get());
                if (yych.get() == ',') return yy17(p);
                if (yych.get() <= '.') return yy7(p);
                if (yych.get() <= '9') return yy17(p);
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
            YYSTATE.set(2);return scan(p);
            case 2:
                yyFillLabel2:
                yych.set((int) 0xff & p.get(p.position()));
                if (yych.get() <= '=') {
                    if (yych.get() <= '/') {
                        if (yych.get() == '-') return yy10(p);
                    } else {
                        if (yych.get() <= '9') return yy10(p);
                        if (yych.get() >= '=') return yy13(p);
                    }
                } else {
                    if (yych.get() <= '^') {
                        if (yych.get() <= '@') return yy12(p);
                        if (yych.get() <= 'Z') return yy10(p);
                    } else {
                        if (yych.get() == '`') return yy12(p);
                        if (yych.get() <= 'z') return yy10(p);
                    }
                }
                yy12:
                {
                    return UTF_8.decode(p).toString();
                }
                yy13:
                yych.set((int) 0xff & (p).get());
                if (yych.get() <= '/') return yy2(p);
                if (yych.get() <= '9') return yy14(p);
                if (yych.get() == '=') return yy16(p);
                return yy2(p);
            yy14:
            skip1(p);
            YYSTATE.set(3);return scan(p);

            case 3:
                yyFillLabel3:
                yych.set((int) 0xff & p.get(p.position()));
                if (yych.get() <= '/') {
                    if (yych.get() == ',') return yy19(p);
                    if (yych.get() <= '.') return yy2(p);
                    return yy18(p);
                } else {
                    if (yych.get() <= '9') return yy14(p);
                    if (yych.get() != '=') return yy2(p);
                }
                yy16:
                skip1(p);
                YYSTATE.set(4);return scan(p);
            case 4:
                yyFillLabel4:
                yych.set((int) 0xff & p.get(p.position()));
                yy17:
                if (yych.get() <= '.') {
                    if (yych.get() == ',') return yy19(p);
                    return yy2(p);
                } else {
                    if (yych.get() <= '/') return yy18(p);
                    if (yych.get() <= '9') return yy16(p);
                    return yy2(p);
                }
                yy18:
                yych.set((int) 0xff & (p).get());
                if (yych.get() <= '/') return yy2(p);
                if (yych.get() <= '9') return yy30(p);
                if (yych.get() == '=') return yy29(p);
                return yy2(p);
            yy19:
            skip1(p);
            YYSTATE.set(5);return scan(p);
            case 5:
                yyFillLabel5:
                yych.set((int) 0xff & p.get(p.position()));
                if (yych.get() <= '/') return yy2(p);
                if (yych.get() <= '9') return yy20(p);
                if (yych.get() == '=') return yy23(p);
                return yy2(p);
            yy20:
            yyaccept.set(3);
            YYMARKER.set(skip1(p));
            YYSTATE.set(6);return scan(p);
            case 6:
                yyFillLabel6:
                yych.set((int) 0xff & p.get(p.position()));
                if (yych.get() <= '/') {
                    if (yych.get() == ',') return yy19(p);
                    if (yych.get() >= '/') return yy25(p);
                } else {
                    if (yych.get() <= '9') return yy20(p);
                    if (yych.get() == '=') return yy23(p);
                }
                yy22:
                {
                    return "found legal range where";
                }
                yy23:
                yyaccept.set(3);
                YYMARKER.set(skip1(p));
                YYSTATE.set(7);return scan(p);
            case 7:
                yyFillLabel7:
                yych.set((int) 0xff & p.get(p.position()));
                if (yych.get() <= '.') {
                    if (yych.get() == ',') return yy19(p);
                    return yy22(p);
                } else {
                    if (yych.get() <= '/') return yy25(p);
                    if (yych.get() <= '9') return yy23(p);
                    return yy22(p);
                }
                yy25:
                skip1(p);
                YYSTATE.set(8);return scan(p);
            case 8:
                yyFillLabel8:
                yych.set((int) 0xff & p.get(p.position()));
                if (yych.get() <= '/') return yy2(p);
                if (yych.get() <= '9') return yy27(p);
                if (yych.get() != '=') return yy2(p);
                skip1(p);
                YYSTATE.set(9);return scan(p);
            case 9:
                yyFillLabel9:
                yych.set((int) 0xff & p.get(p.position()));
                if (yych.get() <= '/') return yy2(p);
                if (yych.get() >= ':') return yy2(p);
                yy27:
                yyaccept.set(3);
                YYMARKER.set(skip1(p));
                YYSTATE.set(10);return scan(p);
            case 10:
                yyFillLabel10:
                yych.set((int) 0xff & p.get(p.position()));
                if (yych.get() == ',') return yy19(p);
                if (yych.get() <= '/') return yy22(p);
                if (yych.get() <= '9') return yy27(p);
                return yy22(p);
            yy29:
            yych.set((int) 0xff & p.get());
            if (yych.get() <= '/') return yy2(p);
            if (yych.get() >= ':') return yy2(p);
            yy30:
            skip1(p);
            YYSTATE.set(11);return scan(p);
            case 11:
                yyFillLabel11:
                yych.set((int) 0xff & p.get(p.position()));
                if (yych.get() == ',') return yy19(p);
                if (yych.get() <= '/') return yy2(p);
                if (yych.get() <= '9') return yy30(p);
                return yy2(p);
        }
        YYSTATE.set(0);return scan(p);
        return scan(p);
        return "";
    }

    private ByteBuffer skip1(ByteBuffer p) {
        return (ByteBuffer) p.position(p.position() + 1);
    }

}

