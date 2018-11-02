package net.macdidi.myandroidtutorial;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.content.Intent;
import java.util.*;
import android.app.Activity;
import android.content.DialogInterface;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    // 宣告資料庫功能類別欄位變數
    private ItemDAO itemDAO;
    
    private GridView item_grid;

    // ListView使用的自定Adapter物件
    private ItemAdapter itemAdapter;
    // 儲存所有記事本的List物件
    private List<Item> items;

    // 選單項目物件
    private MenuItem add_item, search_item, revert_item, delete_item;

    // 已選擇項目數量
    private int selectedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        processViews();
        processControllers();

        // 建立資料庫物件
        itemDAO = new ItemDAO(getApplicationContext());

        /*
        // 如果資料庫是空的，就建立一些範例資料
        // 這是為了方便測試用的，完成應用程式以後可以拿掉
        // ItemDAO有建立範例才使用
        if (itemDAO.getCount() == 0) {
            itemDAO.sample();
        }
        */

        // 取得所有記事資料
        items = itemDAO.getAll();

        itemAdapter = new ItemAdapter(this, R.layout.singleitem, items);
        item_grid.setAdapter(itemAdapter);

        //浮點操作按鈕add動作
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 使用Action名稱建立啟動另一個Activity元件需要的Intent物件
                Intent intent = new Intent("net.macdidi.myandroidtutorial.ADD_ITEM");
                /// 呼叫「startActivityForResult」，，第二個參數「0」表示執行新增
                startActivityForResult(intent, 0);
            }
        });

        Snackbar.make(fab,"歡迎使用方便卡", Snackbar.LENGTH_LONG).setAction("提示", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"請點擊+新增一張新卡",Toast.LENGTH_SHORT).show();
            }
        }).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 如果被啟動的Activity元件傳回確定的結果
        if (resultCode == Activity.RESULT_OK) {
            // 讀取記事物件
            Item item = (Item) data.getExtras().getSerializable(
                    "net.macdidi.myandroidtutorial.Item");

            // 如果是新增記事
            if (requestCode == 0) {
                // 新增記事資料到資料庫
                item = itemDAO.insert(item);

                // 加入新增的記事物件
                items.add(item);

                // 通知資料改變
                itemAdapter.notifyDataSetChanged();
            }

            // 如果是修改記事
             else if (requestCode == 1) {
                // 讀取記事編號
                int position = data.getIntExtra("position", -1);

                if (position != -1) {
                    // 修改資料庫中的記事資料
                    itemDAO.update(item);
                    // 設定修改的記事物件
                    items.set(position, item);
                    itemAdapter.notifyDataSetChanged();
                }
            }

        }
    }
    private void processViews() {
        item_grid = (GridView) findViewById(R.id.grid);
    }

    private void processControllers() {

        AdapterView.OnItemClickListener itemListener = new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // 讀取選擇的記事物件
                Item item = itemAdapter.getItem(position);

                // 如果已經有勾選的項目
                if (selectedCount > 0) {
                    // 處理是否顯示已選擇項目
                    processMenu(item);
                    // 重新設定記事項目
                    itemAdapter.set(position, item);
                }
                else {
                    Intent intent = new Intent(
                            "net.macdidi.myandroidtutorial.EDIT_ITEM");

                    // 設定記事編號與記事物件
                    intent.putExtra("position", position);
                    intent.putExtra("net.macdidi.myandroidtutorial.Item", item);

                    startActivityForResult(intent, 1);
                }
            }
        };


        // 註冊選單項目點擊監聽物件
        item_grid.setOnItemClickListener(itemListener);

        AdapterView.OnItemLongClickListener itemLongListener = new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                // 讀取選擇的記事物件
                Item item = itemAdapter.getItem(position);
                // 處理是否顯示已選擇項目
                processMenu(item);
                // 重新設定記事項目
                itemAdapter.set(position, item);
                return true;
            }

        };

        // 註冊選單項目長按監聽物件
        item_grid.setOnItemLongClickListener(itemLongListener);

    }
    // 處理是否顯示已選擇項目
    private void processMenu(Item item) {
        // 如果需要設定記事項目
        if (item != null) {
            // 設定已勾選的狀態
            item.setSelected(!item.isSelected());

            // 計算已勾選數量
            if (item.isSelected()) {
                selectedCount++;
            }
            else {
                selectedCount--;
            }
        }

        // 根據選擇的狀況，設定是否顯示選單項目
        add_item.setVisible(selectedCount == 0);
        search_item.setVisible(selectedCount == 0);
        revert_item.setVisible(selectedCount > 0);
        delete_item.setVisible(selectedCount > 0);
    }
    // 載入選單資源

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);

        // 取得選單項目物件
        add_item = menu.findItem(R.id.add_item);
        search_item = menu.findItem(R.id.search_item);
        revert_item = menu.findItem(R.id.revert_item);
        delete_item = menu.findItem(R.id.delete_item);


        // 設定選單項目
        processMenu(null);

        return true;
    }

    // 使用者選擇所有的選單項目都會呼叫這個方法
    public void clickMenuItem(MenuItem item) {
            // 使用參數取得使用者選擇的選單項目元件編號
            int itemId = item.getItemId();

            // 判斷該執行什麼工作，目前還沒有加入需要執行的工作
            switch (itemId) {
                case R.id.search_item:
                    break;
                // 使用者選擇新增選單項目
                case R.id.add_item:
                    // 使用Action名稱建立啟動另一個Activity元件需要的Intent物件
                    Intent intent = new Intent("net.macdidi.myandroidtutorial.ADD_ITEM");
                    /// 呼叫「startActivityForResult」，，第二個參數「0」表示執行新增
                    startActivityForResult(intent, 0);
                    break;
                // 取消所有已勾選的項目
                case R.id.revert_item:
                    for (int i = 0; i < itemAdapter.getCount(); i++) {
                        Item ri = itemAdapter.getItem(i);

                        if (ri.isSelected()) {
                            ri.setSelected(false);
                            itemAdapter.set(i, ri);
                        }
                    }

                    selectedCount = 0;
                    processMenu(null);
                    break;
                case R.id.delete_item:
                    // 沒有選擇
                    if (selectedCount == 0) {
                        break;
                    }
                    // 建立與顯示詢問是否刪除的對話框
                    AlertDialog.Builder d = new AlertDialog.Builder(this);
                    String message = getString(R.string.delete_item);;
                    d.setTitle(R.string.delete)
                            .setMessage(String.format(message, selectedCount));
                    d.setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Snackbar.make(getCurrentFocus(),String.format(getString(R.string.deleted_item), selectedCount), Snackbar.LENGTH_LONG).setAction("關閉", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Toast.makeText(MainActivity.this,"已關閉",Toast.LENGTH_SHORT).show();
                                        }
                                    }).show();
                                    // 取得最後一個元素的編號
                                    int index = itemAdapter.getCount() - 1;
                                    while (index > -1) {
                                        Item item = itemAdapter.get(index);

                                        if (item.isSelected()) {
                                            itemAdapter.remove(item);
                                            // 刪除資料庫中的記事資料
                                            itemDAO.delete(item.getId());
                                            selectedCount = 0;
                                        }

                                        index--;
                                    }

                                    itemAdapter.notifyDataSetChanged();
                                }
                            });
                    d.setNegativeButton(android.R.string.no, null);
                    d.show();

                    break;

            }

    }

    // 設定
    public void clickPreferences(MenuItem item) {
        // 啟動設定元件
        startActivity(new Intent(this, PrefActivity.class));
    }
}