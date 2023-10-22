package com.example.calculatortest;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HistoryActivity extends AppCompatActivity {
    private LinearLayout llHistoryRecords;
    private Button btn_delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        llHistoryRecords = findViewById(R.id.ll_history_records);

        loadHistoryRecords(); // 调用加载历史记录的方法

        btn_delete = findViewById(R.id.btn_deleteDB);
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteHistoryRecords();
            }
        });
    }

    private void loadHistoryRecords() {
        CalculatorDatabaseHelper dbHelper = new CalculatorDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {CalculatorDatabaseHelper.COLUMN_FORMULA, CalculatorDatabaseHelper.COLUMN_RESULT};
        // 使用 db.query 方法执行数据库查询
        Cursor cursor = db.query(
                CalculatorDatabaseHelper.TABLE_HISTORY,
                columns,            //要检索的列
                null,               //不添加任何筛选条件
                null,               //选择条件的参数
                null,               //分组方式
                null,               //筛选组的筛选条件
                CalculatorDatabaseHelper.COLUMN_TIMESTAMP + " DESC", // 指定排序方式，按时间戳降序排序，以便最新的历史记录显示在前面。
                "10" // 限制获取的历史记录数量，这里限制为10
        );

        while (cursor.moveToNext()) {
            @SuppressLint("Range") String formula = cursor.getString(cursor.getColumnIndex(CalculatorDatabaseHelper.COLUMN_FORMULA));
            @SuppressLint("Range") String result = cursor.getString(cursor.getColumnIndex(CalculatorDatabaseHelper.COLUMN_RESULT));

            // 创建视图并将其添加到 llHistoryRecords 中
            // 你可以使用 TextView 或 CardView 来显示 formula 和 result
            // 添加点击事件监听器，以便用户可以点击历史记录项进行操作

            // 以下是一个示例，创建一个 TextView 来显示 formula 和 result
            TextView historyItem = new TextView(this);
            historyItem.setText("Formula: " + formula + "\nResult: " + result + "\n");

            // 可以设置 TextView 的样式，如文本颜色、字体大小等
            historyItem.setTextSize(25);
            historyItem.setTextColor(Color.BLACK);

            // 添加点击事件监听器，以便用户可以点击历史记录项进行操作
            historyItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 在这里添加点击历史记录项后的操作
                    // 例如，可以在计算器中重新运行这个历史记录项的计算
                }
            });

            // 将创建的视图添加到 llHistoryRecords 中
            llHistoryRecords.addView(historyItem);
        }
        cursor.close();
        db.close();
    }

    private void deleteHistoryRecords() {
        CalculatorDatabaseHelper dbHelper = new CalculatorDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int rowsDeleted = db.delete(CalculatorDatabaseHelper.TABLE_HISTORY, null, null);

        // 根据 rowsDeleted 的值来判断是否删除成功
        if (rowsDeleted > 0) {
            // 删除成功，可以在界面上清空历史记录列表
            llHistoryRecords.removeAllViews();
        } else {
            // 删除失败，可以显示相应的提示信息
             Toast.makeText(this, "删除历史记录失败", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }


}