package com.example.bmta.utils;
import java.util.LinkedList;

        import android.content.SharedPreferences;
        import android.content.SharedPreferences.Editor;
        import android.text.Editable;
        import android.text.Selection;
        import android.text.TextWatcher;
        import android.text.style.UnderlineSpan;
        import android.widget.TextView;

/**
 * A generic undo/redo implementation for TextViews.
 */
public class TextViewUndoRedo {

    /**
     * Is undo/redo being performed? This member signals if an undo/redo
     * operation is currently being performed. Changes in the text during
     * undo/redo are not recorded because it would mess up the undo history.
     */
    private boolean IsUndoOrRedo = false;

    /**
     * The edit history.
     */
    private final EditHistory EditHistory;

    /**
     * The change listener.
     */
    private final EditTextChangeListener ChangeListener;

    /**
     * The edit text.
     */
    private final TextView TextView;

    // =================================================================== //

    /**
     * Create a new TextViewUndoRedo and attach it to the specified TextView.
     *
     * @param textView
     *            The text view for which the undo/redo is implemented.
     */
    public TextViewUndoRedo(TextView textView) {
        TextView = textView;
        EditHistory = new EditHistory();
        ChangeListener = new EditTextChangeListener();
        TextView.addTextChangedListener(ChangeListener);
    }

    // =================================================================== //

    /**
     * Disconnect this undo/redo from the text view.
     */
    public void disconnect() {
        TextView.removeTextChangedListener(ChangeListener);
    }

    /**
     * Set the maximum history size. If size is negative, then history size is
     * only limited by the device memory.
     */
    public void setMaxHistorySize(int maxHistorySize) {
        EditHistory.setMaxHistorySize(maxHistorySize);
    }

    /**
     * Clear history.
     */
    public void clearHistory() {
        EditHistory.clear();
    }

    /**
     * Can undo be performed?
     */
    public boolean getCanUndo() {
        return (EditHistory.Position > 0);
    }

    /**
     * Perform undo.
     */
    public void undo() {
        EditItem edit = EditHistory.getPrevious();
        if (edit == null) {
            return;
        }

        Editable text = TextView.getEditableText();
        int start = edit.Start;
        int end = start + (edit.After != null ? edit.After.length() : 0);

        IsUndoOrRedo = true;
        text.replace(start, end, edit.Before);
        IsUndoOrRedo = false;

        // This will get rid of underlines inserted when editor tries to come
        // up with a suggestion.

        for (Object o : text.getSpans(0, text.length(), UnderlineSpan.class)) {
            text.removeSpan(o);
        }

        Selection.setSelection(text, edit.Before == null ? start
                : (start + edit.Before.length()));
    }

    /**
     * Can redo be performed?
     */
    public boolean getCanRedo() {
        return (EditHistory.Position < EditHistory.History.size());
    }

    /**
     * Perform redo.
     */
    public void redo() {
        EditItem edit = EditHistory.getNext();
        if (edit == null) {
            return;
        }

        Editable text = TextView.getEditableText();
        int start = edit.Start;
        int end = start + (edit.Before != null ? edit.Before.length() : 0);

        IsUndoOrRedo = true;
        text.replace(start, end, edit.After);
        IsUndoOrRedo = false;

        // This will get rid of underlines inserted when editor tries to come
        // up with a suggestion.
        for (Object o : text.getSpans(0, text.length(), UnderlineSpan.class)) {
            text.removeSpan(o);
        }

        Selection.setSelection(text, edit.After == null ? start
                : (start + edit.After.length()));
    }

    /**
     * Store preferences.
     */
    public void storePersistentState(Editor editor, String prefix) {
        // Store hash code of text in the editor so that we can check if the
        // editor contents has changed.
        editor.putString(prefix + ".hash",
                String.valueOf(TextView.getText().toString().hashCode()));
        editor.putInt(prefix + ".maxSize", EditHistory.MaxHistorySize);
        editor.putInt(prefix + ".position", EditHistory.Position);
        editor.putInt(prefix + ".size", EditHistory.History.size());

        int i = 0;
        for (EditItem ei : EditHistory.History) {
            String pre = prefix + "." + i;

            editor.putInt(pre + ".start", ei.Start);
            editor.putString(pre + ".before", ei.Before.toString());
            editor.putString(pre + ".after", ei.After.toString());

            i++;
        }
    }

    /**
     * Restore preferences.
     *
     * @param prefix
     *            The preference key prefix used when state was stored.
     * @return did restore succeed? If this is false, the undo history will be
     *         empty.
     */
    public boolean restorePersistentState(SharedPreferences sp, String prefix)
            throws IllegalStateException {

        boolean ok = doRestorePersistentState(sp, prefix);
        if (!ok) {
            EditHistory.clear();
        }

        return ok;
    }

    private boolean doRestorePersistentState(SharedPreferences sp, String prefix) {

        String hash = sp.getString(prefix + ".hash", null);
        if (hash == null) {
            return true;
        }

        if (Integer.parseInt(hash) != TextView.getText().toString().hashCode()) {
            return false;
        }

        EditHistory.clear();
        EditHistory.MaxHistorySize = sp.getInt(prefix + ".maxSize", -1);

        int count = sp.getInt(prefix + ".size", -1);
        if (count == -1) {
            return false;
        }

        for (int i = 0; i < count; i++) {
            String pre = prefix + "." + i;

            int start = sp.getInt(pre + ".start", -1);
            String before = sp.getString(pre + ".before", null);
            String after = sp.getString(pre + ".after", null);

            if (start == -1 || before == null || after == null) {
                return false;
            }
            EditHistory.add(new EditItem(start, before, after));
        }

        EditHistory.Position = sp.getInt(prefix + ".position", -1);
        return EditHistory.Position != -1;
    }

    // =================================================================== //

    /**
     * Keeps track of all the edit history of a text.
     */
    private final class EditHistory {

        /**
         * The position from which an EditItem will be retrieved when getNext()
         * is called. If getPrevious() has not been called, this has the same
         * value as History.size().
         */
        private int Position = 0;

        /**
         * Maximum undo history size.
         */
        private int MaxHistorySize = -1;

        /**
         * The list of edits in chronological order.
         */
        private final LinkedList<EditItem> History = new LinkedList<EditItem>();

        /**
         * Clear history.
         */
        private void clear() {
            Position = 0;
            History.clear();
        }

        /**
         * Adds a new edit operation to the history at the current position. If
         * executed after a call to getPrevious() removes all the future history
         * (elements with positions >= current history position).
         */
        private void add(EditItem item) {
            while (History.size() > Position) {
                History.removeLast();
            }
            History.add(item);
            Position++;

            if (MaxHistorySize >= 0) {
                trimHistory();
            }
        }

        /**
         * Set the maximum history size. If size is negative, then history size
         * is only limited by the device memory.
         */
        private void setMaxHistorySize(int maxHistorySize) {
            MaxHistorySize = maxHistorySize;
            if (MaxHistorySize >= 0) {
                trimHistory();
            }
        }

        /**
         * Trim history when it exceeds max history size.
         */
        private void trimHistory() {
            while (History.size() > MaxHistorySize) {
                History.removeFirst();
                Position--;
            }

            if (Position < 0) {
                Position = 0;
            }
        }

        /**
         * Traverses the history backward by one position, returns and item at
         * that position.
         */
        private EditItem getPrevious() {
            if (Position == 0) {
                return null;
            }
            Position--;
            return History.get(Position);
        }

        /**
         * Traverses the history forward by one position, returns and item at
         * that position.
         */
        private EditItem getNext() {
            if (Position >= History.size()) {
                return null;
            }

            EditItem item = History.get(Position);
            Position++;
            return item;
        }
    }

    /**
     * Represents the changes performed by a single edit operation.
     */
    private final class EditItem {
        private final int Start;
        private final CharSequence Before;
        private final CharSequence After;

        /**
         * Constructs EditItem of a modification that was applied at position
         * start and replaced CharSequence before with CharSequence after.
         */
        public EditItem(int start, CharSequence before, CharSequence after) {
            Start = start;
            Before = before;
            After = after;
        }
    }

    /**
     * Class that listens to changes in the text.
     */
    private final class EditTextChangeListener implements TextWatcher {

        /**
         * The text that will be removed by the change event.
         */
        private CharSequence BeforeChange;

        /**
         * The text that was inserted by the change event.
         */
        private CharSequence AfterChange;

        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            if (IsUndoOrRedo) {
                return;
            }

            BeforeChange = s.subSequence(start, start + count);
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            if (IsUndoOrRedo) {
                return;
            }

            AfterChange = s.subSequence(start, start + count);
            EditHistory.add(new EditItem(start, BeforeChange, AfterChange));
        }

        public void afterTextChanged(Editable s) {
        }
    }
}