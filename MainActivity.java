package com.example.calculatortest;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {
    // 按钮控件id
    public static final int[] btnIds = {
            R.id.btn_clear,
            R.id.btn_factorial,
            R.id.btn_percent,
            R.id.btn_division,
            R.id.btn_one,
            R.id.btn_two,
            R.id.btn_three,
            R.id.btn_multiplication,
            R.id.btn_four,
            R.id.btn_five,
            R.id.btn_six,
            R.id.btn_subtraction,
            R.id.btn_seven,
            R.id.btn_eight,
            R.id.btn_nine,
            R.id.btn_addition,
            R.id.btn_zero,
            R.id.btn_dot,
            R.id.btn_equals,
            R.id.btn_copy,
            R.id.btn_sin,
            R.id.btn_cos,
            R.id.btn_tan,
            R.id.btn_history,
            R.id.btn_power
    };
    public EditText editText;
    public TextView text;
    public StringBuilder currentNumber = new StringBuilder();
    public int indexYN = 0;

    private String currentNumberText;       // 存储当前用户输入的数字文本。
    private boolean pressedDotInLast = false;   // 追踪最后一个操作是否是小数点。
    private CalculatorDatabaseHelper databaseHelper;    // 数据库
    private String formula; // 用于跟踪当前的公式
    private String finalResult;  // 用于跟踪当前的结果
    private Context context; // 用于保存传递进来的 context
    private ScaleTouchListener touchListener;
    private TextView txPrecision;   // tv控件 - 显示精确度
    public int precision = 6;      // 保留的小数位数
    private MathUtils mathUtils;
    private Button btn_history;
    private Button btn_copy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化其他内容
        init();

        editText = findViewById(R.id.editView);
        text = findViewById(R.id.textView);

        // 创建数据库帮助类的实例
        databaseHelper = new CalculatorDatabaseHelper(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        touchListener = new ScaleTouchListener();
        mathUtils = new MathUtils();
        btn_history = (Button) findViewById(R.id.btn_history);
        btn_copy = (Button) findViewById(R.id.btn_copy);
        txPrecision = (TextView) findViewById(R.id.tv_precision);
        Button btn;
        formula = "";
        for (int btnId : btnIds) {
            // 遍历按钮ID数组，为每个按钮设置点击事件监听器和触摸事件监听器。
            btn = (Button) findViewById(btnId);
            // 触摸事件监听器：处理的是用户在屏幕上的触摸操作，不仅包括点击，还包括滑动、拖拽、缩放等各种触摸手势。
            // 作用：点击/释放时产生缩小/恢复效果
            btn.setOnTouchListener(touchListener);
            // Q：为什么点击事件可以直接写在onClick函数中，而触摸事件写在另一个类中
            // Ans: 触摸事件处理的方式有多种，因为触摸事件可能涉及更复杂的场景，如拖拽、滑动、缩放等。
            // 为了更灵活地处理这些复杂的交互，通常会创建一个单独的触摸事件监听器类（如你提供的ScaleTouchListener），并将其附加到需要处理触摸事件的视图上。
            // 这种方式允许你以更细粒度的方式处理触摸事件，并且可以在不同的视图元素上重复使用同一个触摸事件监听器。
        }

        txPrecision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(precision > 8)
                    precision = 0;
                precision++;
                mathUtils.setDecimalPlaces(precision);
//                txPrecision.setText(precision);
                txPrecision.setText(String.valueOf(precision));
            }
        });
        btn_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent historyIntent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(historyIntent);
            }
        });
        btn_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 获取剪贴板管理器
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建一个ClipData对象，包含要复制的文本
                ClipData clipData = ClipData.newPlainText("text", finalResult);
                // 将ClipData对象复制到剪贴板
                clipboardManager.setPrimaryClip(clipData);
                // 提示用户文本已复制到剪贴板
                Toast.makeText(getApplicationContext(), "文本已复制到剪贴板", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // 处理按钮点击事件
    public void clickButton(View view) {
        Button button = (Button) view;
        // 将按钮上显示的文本内容追加到名为 editText 的文本编辑框
        editText.append(button.getText());
        // 将按钮上的文本内容追加到名为 str 的 StringBuilder 对象中
        currentNumber.append(button.getText());
        formula += " " + button.getText(); // 将按钮文本添加到 formula 中
    }

    public void emptyButtonClicked(View view) {
        editText.setText(null);
        // 对象的长度设置为 0，实际上是清空了它的内容
        currentNumber.setLength(0);
        formula = "";
        finalResult = "";
    }

    public void equalButtonClicked(View view) {
        indexYN = 0;
        estimate();
        if (indexYN == 0) {

            List<String> infixTokens = tokenizeInfixExpression(currentNumber.toString());
            List<String> houZhui = convertToPostfix(infixTokens);
            editText.append("\n" + calculateExpression(houZhui));
            formula += " =";
            addToHistory(); // 这里将 formula 和 finalResult 添加到数据库
            currentNumber.setLength(0);
            currentNumber.append(calculateExpression(houZhui));

        }
    }

    public void powerButtonClicked(View view) {
        editText.append("^");
        currentNumber.append("^");
        formula += " ^";
    }

    public void eulerButtonClicked(View view) {
        editText.append("e");
        currentNumber.append("e");
        formula += " e";
    }

    public void bracketClicked(View view) {
        Button button = (Button) view;
        // 将按钮上显示的文本内容追加到名为 editText 的文本编辑框
        editText.append(button.getText());
        // 将按钮上的文本内容追加到名为 str 的 StringBuilder 对象中
        currentNumber.append(button.getText());
        formula += " " + button.getText(); // 将按钮文本添加到 formula 中
    }

    public void factorialButtonClicked(View view) {
        editText.append("!");
        currentNumber.append("!");
        formula += " !";
    }

    public void deleteButtonClicked(View view) {
        String nowText = editText.getText().toString();
        if (!nowText.isEmpty() && currentNumber.length() != 0) {
            editText.setText(nowText.substring(0, nowText.length() - 1));
            currentNumber.deleteCharAt(currentNumber.length() - 1);
        }
    }

    public void percentageButtonClicked(View view) {
        editText.append("%");
        currentNumber.append("*0.01");
        formula += " %";
    }

    public void sinButtonClicked(View view) {
        editText.append("sin");
        currentNumber.append("s");
        formula += " sin";
    }

    public void cosButtonClicked(View view) {
        editText.append("cos");
        currentNumber.append("c");
        formula += " cos";
    }

    public void tanButtonClicked(View view) {
        editText.append("tan");
        currentNumber.append("t");
        formula += " tan";
    }

    public void logButtonClicked(View view) {
        editText.append("log");
        currentNumber.append("o");
        formula += " log";
    }

    // 将一个中缀表达式（常见的数学表达式写法，如 "2 + 3"）的字符串转换成一个列表，该列表包含表达式中的各个操作数、操作符和括号等部分。
    private List<String> tokenizeInfixExpression(String infixExpression) {
        int index = 0; // 追踪在字符串 str 中的当前字符位置
        List<String> tokens = new ArrayList<>();    // 创建了一个字符串列表 list，用于存储中缀表达式中的各个部分

        do {
            char currentChar = infixExpression.charAt(index);

            // 是否是中缀表达式中的操作符或括号之一
            if ("+-*/^!logsct()ep".indexOf(currentChar) >= 0) {
                // If the character is an operator or parentheses, add it to the list.
                index++;
                // 将当前字符 currentChar 转换为字符串，并将其添加到 tokens 列表中
                tokens.add(String.valueOf(currentChar));
            } else if ("0123456789".indexOf(currentChar) >= 0) {
                // If the character is a digit, parse the number and add it to the list.
                String number = "";
                while (index < infixExpression.length() && "0123456789.".indexOf(infixExpression.charAt(index)) >= 0) {
                    number += infixExpression.charAt(index);
                    index++;
                }
                tokens.add(number);
            }
        } while (index < infixExpression.length());

        return tokens;
    }


    // 将中缀表达式转换为后缀表达式，也称为逆波兰表达式。
    // 后缀表达式更容易进行计算，因为它不需要括号来明确操作符的优先级
    public List<String> convertToPostfix(List<String> list) {
        // 栈，用于暂时存储操作符和括号，以便进行后缀表达式的转换。
        Stack<String> fuZhan = new Stack<>();
        // 用于存储最终后缀表达式的字符串列表。
        List<String> list2 = new ArrayList<>();
        if (!list.isEmpty()) {
            for (String item : list) {
                if (isNumber(item)) {
                    // 如果 item 是数字（操作数），将其添加到后缀表达式列表 list2 中
                    list2.add(item);
                } else if (isOperator(item)) {
                    if (item.charAt(0) != '(') {// 如果 item 不是左括号 '(',
                        if (fuZhan.isEmpty()) {//如果操作符栈 fuZhan 为空，将 item 推入栈中
                            fuZhan.push(item);
                        } else {
                            // 如果操作符栈不为空，比较栈顶操作符和当前操作符的优先级（由 adv 函数确定）：
                            if (item.charAt(0) != ')') {
                                // 如果当前操作符的优先级大于等于栈顶操作符的优先级，将当前操作符推入栈中
                                if (getOperatorPriority(fuZhan.peek()) <= getOperatorPriority(item)) {
                                    fuZhan.push(item);
                                } else {
                                    // 否则，循环弹出操作符栈中的操作符，并将其添加到后缀表达式列表 list2 中，直到栈顶操作符的优先级小于当前操作符，或者直到栈为空。
                                    while (!fuZhan.isEmpty() && !("(".equals(fuZhan.peek()))) {
                                        if (getOperatorPriority(item) <= getOperatorPriority(fuZhan.peek())) {
                                            list2.add(fuZhan.pop());
                                        }
                                    }
                                    fuZhan.push(item);
                                }
                            } else if (item.charAt(0) == ')') {// 如果 item 是右括号 ')'
                                // 循环弹出操作符栈中的操作符，并将其添加到后缀表达式列表 list2 中，直到遇到左括号 '('。
                                while (!fuZhan.isEmpty() && !("(".equals(fuZhan.peek()))) {
                                    list2.add(fuZhan.pop());
                                }
                                if (!fuZhan.isEmpty() && fuZhan.peek().charAt(0) == '(') {
                                    fuZhan.pop();
                                }
                            }
                        }
                    } else if (item.charAt(0) == '(') {
                        fuZhan.push(item);
                    }
                }
            }
            while (!fuZhan.isEmpty()) {
                list2.add(fuZhan.pop());
            }
        } else {
            editText.setText("");
        }
        return list2;
    }


    public static boolean isOperator(String op) {
        return "0123456789.ep".indexOf(op.charAt(0)) == -1;
    }

    public static boolean isNumber(String num) {
        return "0123456789ep".indexOf(num.charAt(0)) >= 0;
    }

    public static int getOperatorPriority2(String operator) {
        int priority = 0;
        switch (operator) {
            case "+":
            case "-":
                priority = 1;
                break;
            case "*":
            case "/":
                priority = 2;
                break;
            case "^":
            case "!":
            case "g":
            case "l":
            case "o":
            case "s":
            case "c":
            case "t":
                priority = 3;
                break;
        }
        return priority;
    }

    // 定义一个映射表，将操作符映射到它们的优先级
    private static final Map<String, Integer> operatorPriority = new HashMap<>();
    static {
        operatorPriority.put("+", 1);
        operatorPriority.put("-", 1);
        operatorPriority.put("*", 2);
        operatorPriority.put("/", 2);
        operatorPriority.put("^", 3);
        operatorPriority.put("!", 3);
        operatorPriority.put("g", 3);
        operatorPriority.put("l", 3);
        operatorPriority.put("o", 3);
        operatorPriority.put("s", 3);
        operatorPriority.put("c", 3);
        operatorPriority.put("t", 3);
    }
    public static int getOperatorPriority(String f) {
        // 如果操作符在映射表中存在，返回相应的优先级；否则，返回默认值 0
        return operatorPriority.getOrDefault(f, 0);
    }


    public double calculateExpression(List<String> list2) {
        // 存储操作数和中间计算结果
        Stack<String> stack = new Stack<>();
        // 遍历后缀表达式的标记列表 list2，每个标记可以是操作符、操作数或特定的常数
        for (String item : list2) {
            if (isNumber(item)) {
                stack.push(item);
            } else if (isOperator(item)) {
                double res = 0;
                double num1, num2;

                switch (item) {
                    case "e":
                        stack.push(String.valueOf(Math.E));
                        break;
                    case "+":
                        num2 = Double.parseDouble(stack.pop());
                        num1 = Double.parseDouble(stack.pop());
                        res = num1 + num2;
                        res = Double.parseDouble(mathUtils.formatResult(res)); // 格式化结果并重新解析
                        finalResult = res + "";
                        stack.push(String.valueOf(res));
                        break;

                    case "-":
                        num2 = Double.parseDouble(stack.pop());
                        num1 = Double.parseDouble(stack.pop());
                        res = num1 - num2;
                        res = Double.parseDouble(mathUtils.formatResult(res)); // 格式化结果并重新解析
                        finalResult = res + "";
                        stack.push(String.valueOf(res));
                        break;

                    case "*":
                        num2 = Double.parseDouble(stack.pop());
                        num1 = Double.parseDouble(stack.pop());
                        res = num1 * num2;
                        res = Double.parseDouble(mathUtils.formatResult(res)); // 格式化结果并重新解析
                        finalResult = res + "";
                        stack.push(String.valueOf(res));
                        break;

                    case "/":
                        num2 = Double.parseDouble(stack.pop());
                        num1 = Double.parseDouble(stack.pop());
                        if (num2 != 0) {
                            res = num1 / num2;
                            res = Double.parseDouble(mathUtils.formatResult(res)); // 格式化结果并重新解析
                            finalResult = res + "";
                            stack.push(String.valueOf(res));
                        } else {
                            editText.setText("除数不能为0");
                            indexYN = 1;
                        }
                        break;

                    case "^":
                        num2 = Double.parseDouble(stack.pop());
                        num1 = Double.parseDouble(stack.pop());
                        res = Math.pow(num1, num2);
                        res = Double.parseDouble(mathUtils.formatResult(res)); // 格式化结果并重新解析
                        finalResult = res + "";
                        stack.push(String.valueOf(res));
                        break;

                    case "!":
                        num1 = Double.parseDouble(stack.pop());
                        if (num1 == 0 || num1 == 1) {
                            res = 1;
                            stack.push(String.valueOf(res));
                        } else if (num1 == (int) num1 && num1 > 1) {
                            int d = 1;
                            for (int j = (int) num1; j > 0; j--) {
                                d *= j;
                            }
                            res = d;
                            res = Double.parseDouble(mathUtils.formatResult(res)); // 格式化结果并重新解析
                            finalResult = res + "";
                            stack.push(String.valueOf(res));
                        } else {
                            editText.setText("阶乘必须为自然数");
                            indexYN = 1;
                        }
                        break;

                    case "g":
                        num1 = Double.parseDouble(stack.pop());
                        res = Math.sqrt(num1);
                        res = Double.parseDouble(mathUtils.formatResult(res)); // 格式化结果并重新解析
                        finalResult = res + "";
                        stack.push(String.valueOf(res));
                        break;

                    case "l":
                        num1 = Double.parseDouble(stack.pop());
                        if (num1 > 0) {
                            res = Math.log(num1);
                            res = Double.parseDouble(mathUtils.formatResult(res)); // 格式化结果并重新解析
                            finalResult = res + "";
                            stack.push(String.valueOf(res));
                        } else {
                            editText.setText("ln的x必须大于0");
                            indexYN = 1;
                        }
                        break;

                    case "o":
                        num1 = Double.parseDouble(stack.pop());
                        if (num1 > 0) {
                            res = Math.log(num1) / Math.log(2);
                            res = Double.parseDouble(mathUtils.formatResult(res)); // 格式化结果并重新解析
                            finalResult = res + "";
                            stack.push(String.valueOf(res));
                        } else {
                            editText.setText("log的x必须大于0");
                            indexYN = 1;
                        }
                        break;

                    case "s":
                        num1 = Double.parseDouble(stack.pop());
                        res = Math.sin(num1);
                        res = Double.parseDouble(mathUtils.formatResult(res)); // 格式化结果并重新解析
                        finalResult = res + "";
                        stack.push(String.valueOf(res));
                        break;

                    case "c":
                        num1 = Double.parseDouble(stack.pop());
                        res = Math.cos(num1);
                        res = Double.parseDouble(mathUtils.formatResult(res)); // 格式化结果并重新解析
                        finalResult = res + "";
                        stack.push(String.valueOf(res));
                        break;

                    case "t":
                        num1 = Double.parseDouble(stack.pop());
                        if (Math.cos(num1) != 0) {
                            res = Math.tan(num1);
                            res = Double.parseDouble(mathUtils.formatResult(res)); // 格式化结果并重新解析
                            finalResult = res + "";
                            stack.push(String.valueOf(res));
                        } else {
                            editText.setText("tan的x不能为+-(π/2 + kπ)");
                            indexYN = 1;
                        }
                        break;
                }

            }
        }
        if (indexYN == 0) {
            if (!stack.isEmpty()) {
                return Double.parseDouble(stack.pop());
            } else {
                return 0;
            }
        } else {
            return -999999;
        }
    }



    // 检查用户输入的表达式是否合法，它会检测各种不合法的情况并设置 indexYN 标志以指示错误
    public void estimate() {
        text.setText("");
        int i = 0;
        if (currentNumber.length() == 0) {
            text.setText("输入为空！");
            indexYN = 1;
        }
        if (currentNumber.length() == 1) {
            if ("0123456789ep".indexOf(currentNumber.charAt(0)) == -1) {
                text.setText("输入错误！");
                indexYN = 1;
            }
        }
        if (currentNumber.length() > 1) {
            for (i = 0; i < currentNumber.length() - 1; i++) {
                if ("losctg(0123456789ep".indexOf(currentNumber.charAt(0)) == -1) {
                    text.setText("输入错误！");
                    indexYN = 1;
                }
                if ("+-*/".indexOf(currentNumber.charAt(i)) >= 0 && "0123456789losctg(ep".indexOf(currentNumber.charAt(i + 1)) == -1) {
                    text.setText("输入错误！");
                    indexYN = 1;
                }
                if (currentNumber.charAt(i) == '.' && "0123456789".indexOf(currentNumber.charAt(i + 1)) == -1) {
                    text.setText("输入错误！");
                    indexYN = 1;
                }
                if (currentNumber.charAt(i) == '!' && "+-*/^)".indexOf(currentNumber.charAt(i + 1)) == -1) {
                    text.setText("输入错误！");
                    indexYN = 1;
                }
                if ("losctg".indexOf(currentNumber.charAt(i)) >= 0 && "0123456789(ep".indexOf(currentNumber.charAt(i + 1)) == -1) {
                    text.setText("输入错误！");
                    indexYN = 1;
                }
                if (currentNumber.charAt(0) == '0' && currentNumber.charAt(1) == '0') {
                    text.setText("输入错误！");
                    indexYN = 1;
                }
                if (i >= 1 && currentNumber.charAt(i) == '0') {
                    int m = i;
                    int n = i;
                    int is = 0;
                    if ("0123456789.".indexOf(currentNumber.charAt(m - 1)) == -1 && "+-*/.!^)".indexOf(currentNumber.charAt(i + 1)) == -1) {
                        text.setText("输入错误！");
                        indexYN = 1;
                    }
                    if (currentNumber.charAt(m - 1) == '.' && "0123456789+-*/.^)".indexOf(currentNumber.charAt(i + 1)) == -1) {
                        text.setText("输入错误！");
                        indexYN = 1;
                    }
                    n -= 1;
                    while (n > 0) {
                        if ("(+-*/^glosct".indexOf(currentNumber.charAt(n)) >= 0) {
                            break;
                        }
                        if (currentNumber.charAt(n) == '.') {
                            is++;
                        }
                        n--;
                    }
                    if ((is == 0 && currentNumber.charAt(n) == '0') || "0123456789+-*/.!^)".indexOf(currentNumber.charAt(i + 1)) == -1) {
                        text.setText("输入错误！");
                        indexYN = 1;
                    }
                    if (is == 1 && "0123456789+-*/.^)".indexOf(currentNumber.charAt(i + 1)) == -1) {
                        text.setText("输入错误！");
                        indexYN = 1;
                    }
                    if (is > 1) {
                        text.setText("输入错误！");
                        indexYN = 1;
                    }
                }
                if ("123456789".indexOf(currentNumber.charAt(i)) >= 0 && "0123456789+-*/.!^)".indexOf(currentNumber.charAt(i + 1)) == -1) {
                    text.setText("输入错误！");
                    indexYN = 1;
                }
                if (currentNumber.charAt(i) == '(' && "0123456789locstg()ep".indexOf(currentNumber.charAt(i + 1)) == -1) {
                    text.setText("输入错误！");
                    indexYN = 1;
                }
                if (currentNumber.charAt(i) == ')' && "+-*/!^)".indexOf(currentNumber.charAt(i + 1)) == -1) {
                    text.setText("输入错误！");
                    indexYN = 1;
                }
                if ("0123456789!)ep".indexOf(currentNumber.charAt(currentNumber.length() - 1)) == -1) {
                    text.setText("输入错误！");
                    indexYN = 1;
                }
                if (i > 2 && currentNumber.charAt(i) == '.') {
                    int n = i - 1;
                    int is = 0;
                    while (n > 0) {
                        if ("(+-*/^glosct".indexOf(currentNumber.charAt(n)) >= 0) {
                            break;
                        }
                        if (currentNumber.charAt(n) == '.') {
                            is++;
                        }
                        n--;
                    }
                    if (is > 0) {
                        text.setText("输入错误！");
                        indexYN = 1;
                    }
                }
                if ("ep".indexOf(currentNumber.charAt(i)) >= 0 && "+-*/^)".indexOf(currentNumber.charAt(i + 1)) == -1) {
                    text.setText("输入错误！");
                    indexYN = 1;
                }
            }
        }
    }



    // 添加方法来将公式和结果添加到数据库
    public void addToHistory() {
        if (formula != null && finalResult != null) {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(CalculatorDatabaseHelper.COLUMN_FORMULA, formula);
            values.put(CalculatorDatabaseHelper.COLUMN_RESULT, finalResult);
            long newRowId = db.insert(CalculatorDatabaseHelper.TABLE_HISTORY, null, values);
            db.close();
        }
    }
}