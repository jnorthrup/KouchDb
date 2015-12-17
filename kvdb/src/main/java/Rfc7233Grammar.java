import bbcursive.Cursive;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jim on 12/15/2015.
 */
public class Rfc7233Grammar implements UnaryOperator<ByteBuffer> {

    private static final Pattern NOSPACES = Pattern.compile("\\b+");
    private static final Pattern COMPILE = Pattern.compile("^(\\w+=)?\\d*-\\d*(,\\d*-\\d*)*$");

    
    ThreadLocal<Integer> YYSTATE=ThreadLocal.withInitial(() -> -1); 
    String scan(ByteBuffer p)
    {


        switch (YYSTATE.get()) {
            case 0: continue yyFillLabel0;
            case 1: continue yyFillLabel1;
            case 2: continue yyFillLabel2;
            case 3: continue yyFillLabel3;
            case 4: continue yyFillLabel4;
            case 5: continue yyFillLabel5;
            case 6: continue yyFillLabel6;
            case 7: continue yyFillLabel7;
            case 8: continue yyFillLabel8;
            case 9: continue yyFillLabel9;
            case 10: continue yyFillLabel10;
            case 11: continue yyFillLabel11;
            default: continue yy0;
                YYSTATE.set(0);
        }
        yyFillLabel0:
        yych = (unsigned char)*p;
        if (yych <= '=') {
            if (yych <= '.') {
                if (yych == '-') continue yy10;
            } else {
                if (yych <= '/') continue yy8;
                if (yych <= '9') continue yy3;
                if (yych >= '=') continue yy6;
            }
        } else {
            if (yych <= '^') {
                if (yych <= '@') continue yy2;
                if (yych <= 'Z') continue yy10;
            } else {
                if (yych == '`') continue yy2;
                if (yych <= 'z') continue yy10;
            }
        }
        yy2:
        p = YYMARKER;
        if (yyaccept <= 1) {
            if (yyaccept <= 0) {
                continue yy5;
            } else {
                continue yy7;
            }
        } else {
            if (yyaccept <= 2) {
                continue yy12;
            } else {
                continue yy22;
            }
        }
        yy3:
        yyaccept = 0;
        YYMARKER = ++p;
        YYSTATE.set(1);
        yyFillLabel1:
        yych = (unsigned char)*p;
        if (yych <= '<') {
            if (yych <= '-') {
                if (yych <= '+') continue yy5;
                if (yych <= ',') continue yy19;
                continue yy10;
            } else {
                if (yych <= '.') continue yy5;
                if (yych <= '/') continue yy18;
                if (yych <= '9') continue yy3;
            }
        } else {
            if (yych <= '^') {
                if (yych <= '=') continue yy14;
                if (yych <= '@') continue yy5;
                if (yych <= 'Z') continue yy10;
            } else {
                if (yych == '`') continue yy5;
                if (yych <= 'z') continue yy10;
            }
        }
        yy5:
        {return p;}
        yy6:
        yyaccept = 1;
        yych = (unsigned char)*(YYMARKER = ++p);
        if (yych == ',') continue yy17;
        if (yych <= '.') continue yy7;
        if (yych <= '9') continue yy17;
        yy7:
        {return "onward to";}
        yy8:
        ++p;
        {return "of";}
        yy10:
        yyaccept = 2;
        YYMARKER = ++p;
        YYSTATE.set(2);
        yyFillLabel2:
        yych = (unsigned char)*p;
        if (yych <= '=') {
            if (yych <= '/') {
                if (yych == '-') continue yy10;
            } else {
                if (yych <= '9') continue yy10;
                if (yych >= '=') continue yy13;
            }
        } else {
            if (yych <= '^') {
                if (yych <= '@') continue yy12;
                if (yych <= 'Z') continue yy10;
            } else {
                if (yych == '`') continue yy12;
                if (yych <= 'z') continue yy10;
            }
        }
        yy12:
        {return p;}
        yy13:
        yych = (unsigned char)*++p;
        if (yych <= '/') continue yy2;
        if (yych <= '9') continue yy14;
        if (yych == '=') continue yy16;
        continue yy2;
        yy14:
        ++p;
        YYSTATE.set(3);
        yyFillLabel3:
        yych = (unsigned char)*p;
        if (yych <= '/') {
            if (yych == ',') continue yy19;
            if (yych <= '.') continue yy2;
            continue yy18;
        } else {
            if (yych <= '9') continue yy14;
            if (yych != '=') continue yy2;
        }
        yy16:
        ++p;
        YYSTATE.set(4);
        yyFillLabel4:
        yych = (unsigned char)*p;
        yy17:
        if (yych <= '.') {
            if (yych == ',') continue yy19;
            continue yy2;
        } else {
            if (yych <= '/') continue yy18;
            if (yych <= '9') continue yy16;
            continue yy2;
        }
        yy18:
        yych = (unsigned char)*++p;
        if (yych <= '/') continue yy2;
        if (yych <= '9') continue yy30;
        if (yych == '=') continue yy29;
        continue yy2;
        yy19:
        ++p;
        YYSTATE.set(5);
        yyFillLabel5:
        yych = (unsigned char)*p;
        if (yych <= '/') continue yy2;
        if (yych <= '9') continue yy20;
        if (yych == '=') continue yy23;
        continue yy2;
        yy20:
        yyaccept = 3;
        YYMARKER = ++p;
        YYSTATE.set(6);
        yyFillLabel6:
        yych = (unsigned char)*p;
        if (yych <= '/') {
            if (yych == ',') continue yy19;
            if (yych >= '/') continue yy25;
        } else {
            if (yych <= '9') continue yy20;
            if (yych == '=') continue yy23;
        }
        yy22:
        { return "found legal range where"}
        yy23:
        yyaccept = 3;
        YYMARKER = ++p;
        YYSTATE.set(7);
        yyFillLabel7:
        yych = (unsigned char)*p;
        if (yych <= '.') {
            if (yych == ',') continue yy19;
            continue yy22;
        } else {
            if (yych <= '/') continue yy25;
            if (yych <= '9') continue yy23;
            continue yy22;
        }
        yy25:
        ++p;
        YYSTATE.set(8);
        yyFillLabel8:
        yych = (unsigned char)*p;
        if (yych <= '/') continue yy2;
        if (yych <= '9') continue yy27;
        if (yych != '=') continue yy2;
        ++p;
        YYSTATE.set(9);
        yyFillLabel9:
        yych = (unsigned char)*p;
        if (yych <= '/') continue yy2;
        if (yych >= ':') continue yy2;
        yy27:
        yyaccept = 3;
        YYMARKER = ++p;
        YYSTATE.set(10);
        yyFillLabel10:
        yych = (unsigned char)*p;
        if (yych == ',') continue yy19;
        if (yych <= '/') continue yy22;
        if (yych <= '9') continue yy27;
        continue yy22;
        yy29:
        yych = (unsigned char)*++p;
        if (yych <= '/') continue yy2;
        if (yych >= ':') continue yy2;
        yy30:
        ++p;
        YYSTATE.set(11);
        yyFillLabel11:
        yych = (unsigned char)*p;
        if (yych == ',') continue yy19;
        if (yych <= '/') continue yy2;
        if (yych <= '9') continue yy30;
        continue yy2;

    }


}