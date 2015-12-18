import java.nio.ByteBuffer;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Rfc7233Grammar {
    private static final Pattern NOSPACES = Pattern.compile("\\b+");
    private static final Pattern COMPILE = Pattern.compile("^(\\w+=)?\\d*-\\d*(,\\d*-\\d*)*$");
    static ThreadLocal<ByteBuffer> YYMARKER = new ThreadLocal<>();
    static ThreadLocal<Integer> yyaccept = new ThreadLocal<>();
    static ThreadLocal<Integer> yych = new ThreadLocal<>();
    static ThreadLocal<Integer> YYSTATE = ThreadLocal.withInitial(() -> -1);


    public String scan(ByteBuffer p) {
        try {
            goto0:
            while (true)
                switch ((int) YYSTATE.get()) {
                    case 0: {
                        yych.set((int) 0xff & p.get(p.position()));
                        if (yych.get() <= '=') if (yych.get() > '.') {
                            if (yych.get() <= '^') {
                                if (yych.get() <= '@') {
                                    yy2(p);
                                    continue goto0;
                                } else if (yych.get() <= 'Z') {
                                    yy10(p);
                                    continue goto0;
                                }
                                ;
                            } else {
                                if (yych.get() == '`') {
                                    yy2(p);
                                    continue goto0;
                                } else if (yych.get() <= 'z') {
                                    yy10(p);
                                    continue goto0;
                                }
                                ;
                            }
                        } else if (yych.get() == '-') {
                            yy10(p);
                            continue goto0;
                        } else if (yych.get() <= '/') {
                            yy8(p);
                            continue goto0;
                        } else if (yych.get() <= '9') {
                            yy3(p);
                            continue goto0;
                        } else if (yych.get() >= '=') {
                            yy6(p);
                            continue goto0;
                        }
                    }
                    p = yy2(p);
                    case 1:
                        yyFillLabel1:
                        yych.set((int) 0xff & p.get(p.position()));
                        if (yych.get() <= '<') {
                            if (yych.get() <= '-') {
                                if (yych.get() <= '+') {
                                    yy5(p);
                                    continue goto0;
                                } else if (yych.get() <= ',') {
                                    yy19(p);
                                    continue goto0;
                                } else {
                                    yy10(p);
                                    continue goto0;
                                }

                            } else {
                                if (yych.get() <= '.') {
                                    yy5(p);
                                    continue goto0;
                                } else if (yych.get() <= '/') {
                                    yy18(p);
                                    continue goto0;
                                } else if (yych.get() <= '9') {
                                    yy3(p);
                                    continue goto0;
                                }
                            }
                        } else {
                            if (yych.get() <= '^') {
                                if (yych.get() <= '=') {
                                    yy14(p);
                                    continue goto0;
                                } else if (yych.get() <= '@') {
                                    yy5(p);
                                    continue goto0;
                                } else if (yych.get() <= 'Z') {
                                    yy10(p);
                                    continue goto0;
                                }
                            } else {
                                if (yych.get() == '`') {
                                    yy5(p);
                                    continue goto0;
                                } else if (yych.get() <= 'z') {
                                    yy10(p);
                                    continue goto0;
                                }
                            }
                        }
                        return yy5(p);


                    case 2:
                        yyFillLabel2:
                        yych.set((int) 0xff & p.get(p.position()));
                        if (yych.get() <= '=') {
                            if (yych.get() <= '/') {
                                if (yych.get() == '-') {
                                    yy10(p);
                                    continue goto0;
                                }
                            } else {
                                if (yych.get() <= '9') {
                                    yy10(p);
                                    continue goto0;
                                } else if (yych.get() >= '=') {
                                    yy13(p);
                                    continue goto0;
                                }
                            }
                        } else {
                            if (yych.get() <= '^') {
                                if (yych.get() <= '@') {
                                    yy12(p);
                                    continue goto0;
                                } else if (yych.get() <= 'Z') {
                                    yy10(p);
                                    continue goto0;
                                }
                            } else if (yych.get() == '`') {
                                yy12(p);
                                continue goto0;
                            } else if (yych.get() <= 'z') {
                                yy10(p);
                                continue goto0;
                            }
                        }
                        return yy12(p);
                    case 3:
                        yyFillLabel3:
                        yych.set((int) 0xff & p.get(p.position()));
                        if (yych.get() <= '/') {
                            if (yych.get() == ',') {
                                yy19(p);
                                continue goto0;
                            } else if (yych.get() <= '.') {
                                yy2(p);
                                continue goto0;
                            } else {
                                yy18(p);
                                continue goto0;
                            }
                        } else {
                            if (yych.get() <= '9') {
                                yy14(p);
                                continue goto0;
                            } else if (yych.get() != '=') {
                                yy2(p);
                                continue goto0;
                            }
                            ;
                        }
                        yy16(p);
                    case 4:
                        yyFillLabel4:
                        yych.set((int) 0xff & p.get(p.position()));
                        yy17(p);
                        continue goto0;
                    case 5:
                        yyFillLabel5:
                        yych.set((int) 0xff & p.get(p.position()));
                        if (yych.get() <= '/') {
                            yy2(p);
                            continue goto0;
                        } else if (yych.get() <= '9') {
                            yy20(p);
                            continue goto0;
                        } else if (yych.get() == '=') {
                            yy23(p);
                            continue goto0;
                        } else {
                            yy2(p);
                            continue goto0;
                        }
                    case 6:
                        yyFillLabel6:
                        yych.set((int) 0xff & p.get(p.position()));
                        if (yych.get() <= '/') {
                            if (yych.get() == ',') {
                                yy19(p);
                                continue goto0;
                            } else if (yych.get() >= '/') {
                                yy25(p);
                                continue goto0;
                            }
                            ;
                        } else {
                            if (yych.get() <= '9') {
                                yy20(p);
                                continue goto0;
                            } else if (yych.get() == '=') {
                                yy23(p);
                                continue goto0;
                            }
                            ;
                        }
                        return yy22(p);
                    case 7:
                        yyFillLabel7:
                        yych.set((int) 0xff & p.get(p.position()));
                        if (yych.get() <= '.') {
                            if (yych.get() == ',') {
                                yy19(p);
                                continue goto0;
                            } else {
                                yy22(p);
                                continue goto0;
                            }
                        } else {
                            if (yych.get() <= '/') {
                                yy25(p);
                                continue goto0;
                            } else if (yych.get() <= '9') {
                                yy23(p);
                                continue goto0;
                            } else {
                                yy22(p);
                                continue goto0;
                            }
                        }
                    case 8:
                        yyFillLabel8:
                        yych.set((int) 0xff & p.get(p.position()));
                        if (yych.get() <= '/') {
                            yy2(p);
                            continue goto0;
                        } else if (yych.get() <= '9') {
                            yy27(p);
                            continue goto0;
                        } else if (yych.get() != '=') {
                            yy2(p);
                            continue goto0;
                        } else
                            skip1(p);
                        YYSTATE.set(9);
                    case 9:
                        yyFillLabel9:
                        yych.set((int) 0xff & p.get(p.position()));
                        if (yych.get() <= '/') {
                            yy2(p);
                            continue goto0;
                        } else if (yych.get() >= ':') {
                            yy2(p);
                            continue goto0;
                        } else {
                            yy27(p);
                        }
                    case 10:
                        yyFillLabel10:
                        yych.set((int) 0xff & p.get(p.position()));
                        if (yych.get() == ',') {
                            yy19(p);
                            continue goto0;
                        } else if (yych.get() <= '/') {
                            yy22(p);
                            continue goto0;
                        } else if (yych.get() <= '9') {
                            yy27(p);
                            continue goto0;
                        } else {
                            yy22(p);
                            continue goto0;
                        }
                    case 11:
                        yyFillLabel11:
                        yych.set((int) 0xff & p.get(p.position()));
                        if (yych.get() == ',') {
                            yy19(p);
                            continue goto0;
                        } else if (yych.get() <= '/') {
                            yy2(p);
                            continue goto0;
                        } else if (yych.get() <= '9') {
                            yy30(p);
                            continue goto0;
                        } else {
                            yy2(p);
                            continue goto0;
                        }
                }
        } catch (ResultException e) {
            String message = e.getMessage();
            return message;
        }
    }

    private ByteBuffer yy2(ByteBuffer p) throws ResultException {
        yy2:
        p = YYMARKER.get();
        yyaccept.set(0);
        if (yyaccept.get() <= 1) if (yyaccept.get() <= 0) {
            yy5(p);
            return p;
        } else {
            yy7(p);
            return p;
        } else if (yyaccept.get() <= 2) {
            yy12(p);
            return p;
        } else {
            yy22(p);
            return p;
        }
    }

    private void yy3(ByteBuffer p) {
        yy3:
        yyaccept.set(0);
        YYMARKER.set(skip1(p));
        YYSTATE.set(1);
    }

    private String yy5(ByteBuffer p) throws ResultException {
        yy5:
        {
            throw new ResultException(String.valueOf(UTF_8.decode(p)));
        }
    }

    private String yy8(ByteBuffer p) throws ResultException {
        yy8:
        skip1(p);
        {
            throw new ResultException("of");
        }
    }

    private void yy6(ByteBuffer p) throws ResultException {
        yy6:
        yyaccept.set(1);
        YYMARKER.set(p.duplicate());
        yych.set(0xff & p.get());
        if (yych.get() == ',') {
            yy17(p);
            return;
        } else if (yych.get() > '.') {
            if (yych.get() <= '9') {
                yy17(p);
                return;
            }
        }
        yy7(p);
    }

    private void yy7(ByteBuffer p) throws ResultException {
        yy7:
        {
            throw new ResultException("onward to");
        }
    }

    private void yy10(ByteBuffer p) {
        yy10:
        yyaccept.set(2);
        YYMARKER.set(skip1(p));
        YYSTATE.set(2);
    }

    private String yy12(ByteBuffer p) throws ResultException {
        yy12:
        {
            throw new ResultException(UTF_8.decode(p).toString());
        }
    }

    private void yy13(ByteBuffer p) throws ResultException {
        yy13:
        yych.set((int) 0xff & (p).get());
        if (yych.get() <= '/') {
            yy2(p);
            return;
        } else if (yych.get() <= '9') {
            yy14(p);
            return;
        } else if (yych.get() == '=') {
            yy16(p);
            return;
        } else {
            yy2(p);
            return;
        }
    }

    private void yy14(ByteBuffer p) {
        yy14:
        skip1(p);
        YYSTATE.set(3);
    }

    private void yy16(ByteBuffer p) {
        yy16:
        skip1(p);
        YYSTATE.set(4);
    }

    private void yy17(ByteBuffer p) throws ResultException {
        yy17:
        if (yych.get() > '.') {
            if (yych.get() <= '/') {
                yy18(p);
                return;
            }
            if (yych.get() <= '9') {
                yy16(p);
                return;
            }
            yy2(p);
            return;

        }
        if (yych.get() == ',') {
            yy19(p);
            return;
        }
        yy2(p);
        return;
    }

    private void yy18(ByteBuffer p) throws ResultException {
        yy18:
        yych.set((int) 0xff & (p).get());
        if (yych.get() <= '/') {
            yy2(p);
            return;
        } else if (yych.get() <= '9') {
            yy30(p);
            return;
        } else if (yych.get() == '=') {
            yy29(p);
            return;
        } else {
            yy2(p);
            return;
        }


    }

    private void yy19(ByteBuffer p) {
        yy19:
        skip1(p);
        YYSTATE.set(5);
    }

    private void yy20(ByteBuffer p) {
        yy20:
        yyaccept.set(3);
        YYMARKER.set(skip1(p));
        YYSTATE.set(6);
    }

    private String yy22(ByteBuffer p) throws ResultException {
        yy22:
        {
            throw new ResultException("found legal range where");
        }
    }

    private void yy23(ByteBuffer p) {
        yy23:
        yyaccept.set(3);
        YYMARKER.set(skip1(p));
        YYSTATE.set(7);
    }

    private void yy25(ByteBuffer p) {
        yy25:
        skip1(p);
        YYSTATE.set(8);
    }

    private void yy27(ByteBuffer p) {
        yy27:
        yyaccept.set(3);
        YYMARKER.set(skip1(p));
        YYSTATE.set(10);
    }

    private boolean yy29(ByteBuffer p) throws ResultException {
        yy29:
        yych.set((int) 0xff & p.get());
        if (yych.get() <= '/') {
            yy2(p);
            return true;
        }
        ;
        if (yych.get() >= ':') {
            yy2(p);
            return true;
        }
        ;
        yy30(p);
        return false;
    }

    private void yy30(ByteBuffer p) {
        yy30:
        skip1(p);
        YYSTATE.set(11);
    }

    private ByteBuffer skip1(ByteBuffer p) {
        return (ByteBuffer) p.position(p.position() + 1);
    }


    public class ResultException extends Exception {
        public ResultException(String s) {
            super(s);
        }
    }
}

