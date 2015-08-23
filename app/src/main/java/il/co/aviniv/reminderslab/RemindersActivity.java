package il.co.aviniv.reminderslab;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


public class RemindersActivity extends AppCompatActivity {

    private ListView mListView;
    private RemindersDbAdapter mDbAdapter;
    private RemindersSimpleCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);

        try {
            mListView = (ListView) findViewById(R.id.reminders_list_view);
            mListView.setDivider(null);

            mDbAdapter = new RemindersDbAdapter(this);
            mDbAdapter.open();

            //fill sample data if new instance
            if (savedInstanceState == null) {
                addSampleData(mDbAdapter);
            }

            Cursor cursor = mDbAdapter.fetchAllReminders();

            //from columns defined in the db
            String[] from = new String[]{RemindersDbAdapter.COL_CONTENT};
            //to the ids of views in the layout
            int[] to = new int[]{R.id.row_text};

            mCursorAdapter = new RemindersSimpleCursorAdapter(RemindersActivity.this, R.layout.reminders_row, cursor, from, to, 0);
            //( context , the layout of the row , cursor , from columns defined in the db , to the ids of views in the layout , flag - not used );

            //the cursorAdapter (controller) is now updating the listView (view) with data from the db (model)
            mListView.setAdapter(mCursorAdapter);


            //this script if for static list, replaced by the above dynamic list
            /*------------------------------------------------------------------------------------------------//
            //The arrayAdatper is the controller in our model-view-controller relationship. (controller)
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    this, R.layout.reminders_row, R.id.row_text, new String[]{"first record", "second record", "third record"});
                    //(context, layout (view), row (view), data (model) with bogus data to test our listview);
            mListView.setAdapter(arrayAdapter);
            //------------------------------------------------------------------------------------------------*/


            //when we click an individual item in the listview
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int masterListPosition, long id){
                    //create a dialog which contains 2 views (edit, delete)
                    AlertDialog.Builder builder = new AlertDialog.Builder(RemindersActivity.this);
                    ListView modeListView = new ListView(RemindersActivity.this);
                    String[] modes = new String[] { "Edit Reminder", "Delete Reminder" };
                    //create an inner view for each option to select (edit, delete)
                    ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(
                            RemindersActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, modes);
                    modeListView.setAdapter(modeAdapter);
                    builder.setView(modeListView);
                    final Dialog dialog = builder.create();
                    dialog.show();

                    //create an onclick listener to the inner view (modeListView), to handle selected option action
                    modeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if (position == 0) {
                                //edit reminder. 0 = the first item in the popup box.
                                int nId = getIdFromPosition(masterListPosition);
                                Reminder reminder = mDbAdapter.fetchReminderById(nId);
                                fireCustomDialog(reminder);
                            } else {
                                //delete reminder. 1 = the second item in the popup box.
                                mDbAdapter.deleteReminderById(getIdFromPosition(masterListPosition));
                                mCursorAdapter.changeCursor(mDbAdapter.fetchAllReminders());
                            }
                            dialog.dismiss();
                        }
                    });

                    //this script if for static list, replaced by the above script
                    /*------------------------------------------------------------------------------------------------//
                    //create an onclick listener to the inner view (modeListView), to handle selected option action
                    modeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            //edit reminder
                            if (position == 0) {
                                Toast.makeText(RemindersActivity.this, "edit " + masterListPosition, Toast.LENGTH_SHORT).show();
                            }
                            //delete reminder
                            else {
                                Toast.makeText(RemindersActivity.this, "delete " + masterListPosition, Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                        }
                    });
                    //------------------------------------------------------------------------------------------------*/
                };

                //this script if for testing the position
                /*@Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(RemindersActivity.this, "clicked " + position, Toast.LENGTH_SHORT).show();
                }*/
            });


            //check version for new features compitability
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
                mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

                    private int totalSelected = 0;

                    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                    @Override
                    // Here you can do something when items are selected/de-selected, such as update the title in the CAB
                    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                        if (checked) {totalSelected++;}
                        else {totalSelected--;}
                        mode.setTitle(String.valueOf(totalSelected) + " items selected");
                    }

                    @TargetApi(Build.VERSION_CODES.HONEYCOMB)   //added by the android studio
                    @Override
                    // Respond to clicks on the actions in the CAB
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        totalSelected = 0;
                        MenuInflater inflater = mode.getMenuInflater();
                        inflater.inflate(R.menu.cam_menu, menu);                //inflate a context menu in the action bar
                        return true;                                            //true = enter multi select action mode
                    }

                    @Override
                    // Here you can perform updates to the CAB due to an invalidate() request
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    @TargetApi(Build.VERSION_CODES.HONEYCOMB)   //added by the android studio
                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item)  {
                        try {
                            //select or deselect each item in context menu
                            switch (item.getItemId()) {
                                case R.id.menu_item_delete_reminder:
                                    for (int nC = mCursorAdapter.getCount() - 1; nC >= 0; nC--) {
                                        if (mListView.isItemChecked(nC)) {
                                            mDbAdapter.deleteReminderById(getIdFromPosition(nC));
                                        }
                                    }
                                    mode.finish();  //end multi select mode
                                    mCursorAdapter.changeCursor(mDbAdapter.fetchAllReminders());
                                    return true;        // true = the action has been properly handled
                            }
                        }
                        catch (Exception ex) {
                            Log.e("ANIVE", ex.getMessage());
                        }
                        return false;
                    }

                    @Override
                    // Here you can make any necessary updates to the activity when the CAB is removed. By default, selected items are deselected/unchecked.
                    public void onDestroyActionMode(ActionMode mode) { }

                });
            }
        }
        catch (Exception ex) {
            Utilities.showException(this, ex);
        }
    }

    private int getIdFromPosition(int nC) {
        return (int)mCursorAdapter.getItemId(nC);
    }

    private void fireCustomDialog(final Reminder reminder) {
        //custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_custom);

        TextView titleView = (TextView) dialog.findViewById(R.id.custom_title);
        final EditText editCustom = (EditText) dialog.findViewById(R.id.custom_edit_reminder);
        Button commitButton = (Button) dialog.findViewById(R.id.custom_button_commit);
        Button buttonCancel = (Button) dialog.findViewById(R.id.custom_button_cancel);
        final CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.custom_check_box);
        LinearLayout rootLayout = (LinearLayout) dialog.findViewById(R.id.custom_root_layout);
        final boolean isEditOperation = (reminder != null);

        //this is for edit
        if (isEditOperation) {
            titleView.setText(R.string.edit_reminder);
            checkBox.setChecked(reminder.getImportant() == 1);
            editCustom.setText(reminder.getContent());
            rootLayout.setBackgroundColor(getResources().getColor(R.color.blue));
        }

        //set commit button
        commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String reminderText = editCustom.getText().toString();
                if (isEditOperation) {
                    //existing reminder
                    Reminder reminderEdited = new Reminder(reminder.getId(), reminderText, checkBox.isChecked() ? 1 : 0);
                    mDbAdapter.updateReminder(reminderEdited);
                } else {
                    //new reminder
                    mDbAdapter.createReminder(reminderText, checkBox.isChecked());
                }
                mCursorAdapter.changeCursor(mDbAdapter.fetchAllReminders());;
                dialog.dismiss();
            }
        });

        //set cancel button
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reminders, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*// Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);*/

        switch (item.getItemId()) {
            case R.id.action_new:
                //create new reminder
                //Log.d(getLocalClassName(), "create new reminder");    >>replaced
                fireCustomDialog(null);
                return true;
            case R.id.action_exit:
                finish();
                return true;
            default:
                return false;
        }
    }

    private void addSampleData(RemindersDbAdapter mDbAdapter) {
        try {
            //Clear all data
            mDbAdapter.deleteAllReminders();
            //Add some data
            insertSomeReminders(mDbAdapter);
        }
        catch (Exception ex) {
            Utilities.showException(this, ex);
        }
    }

    private void insertSomeReminders(RemindersDbAdapter mDbAdapter) {
        try {
            mDbAdapter.createReminder("Buy Learn Android Studio", true);
            mDbAdapter.createReminder("Send Dad birthday gift", false);
            mDbAdapter.createReminder("Dinner at the Gage on Friday", false);
            mDbAdapter.createReminder("String squash racket", false);
            mDbAdapter.createReminder("Shovel and salt walkways", false);
            mDbAdapter.createReminder("Prepare Advanced Android syllabus", true);
            mDbAdapter.createReminder("Buy new office chair", false);
            mDbAdapter.createReminder("Call Auto-body shop for quote", false);
            mDbAdapter.createReminder("Renew membership to club", false);
            mDbAdapter.createReminder("Buy new Galaxy Android phone", true);
            mDbAdapter.createReminder("Sell old Android phone - auction", false);
            mDbAdapter.createReminder("Buy new paddles for kayaks", false);
            mDbAdapter.createReminder("Call accountant about tax returns", false);
            mDbAdapter.createReminder("Buy 300,000 shares of Google", false);
            mDbAdapter.createReminder("Call the Dalai Lama back", true);
        }
        catch (Exception ex) {
            Utilities.showException(this, ex);
        }
    }
}
