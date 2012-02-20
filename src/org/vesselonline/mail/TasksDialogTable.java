package org.vesselonline.mail;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import com.google.api.services.tasks.model.Task;

public class TasksDialogTable extends JTable {
  /**
   * For Serializable interface, value generated by Eclipse.
   */
  private static final long serialVersionUID = 3786875951814415933L;

  private TasksDisplay tasksDisplay;

  public TasksDialogTable(final TasksDisplay tasksDisplay) {
    super(tasksDisplay.getTableModel());
    this.tasksDisplay = tasksDisplay;

    this.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          new TasksDialog(tasksDisplay.getTasks().getItems().get(rowAtPoint(e.getPoint())));
        }
      }
    });
  }

  private class TasksDialog extends JDialog {
    /**
     * For Serializable interface, value generated by Eclipse.
     */
    private static final long serialVersionUID = 8894082783005282337L;

    private static final String DIALOG_TITLE = "Google Tasks";

    public TasksDialog(final Task task) {
      super();

      setTitle(DIALOG_TITLE);
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);

      final JTextField taskTitleTextField = new JTextField(task.getTitle(), 48);

      final JTextArea taskNotesTextArea = new JTextArea(task.getNotes(), 16, 48);
      taskNotesTextArea.setLineWrap(true);

      final JPanel buttonPanel = new JPanel();

      final JButton saveButton = new JButton("Save");
      saveButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setVisible(false);

          try {
            task.setTitle(taskTitleTextField.getText());
            task.setNotes(taskNotesTextArea.getText());

            DailyBriefingUtils.INSTANCE.getGoogleTasksService().tasks().update(tasksDisplay.getTaskListID(), task.getId(), task).execute();

            tasksDisplay.refresh();

          } catch (IOException ioe) {
            handleException(ioe, task);
          } catch (DailyBriefingException dbe) {
            handleException(dbe, task);
          }

          dispose();
        }
      });

      final JButton cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          dispose();
        }
      });

      buttonPanel.add(saveButton);
      buttonPanel.add(cancelButton);

      add(taskTitleTextField, BorderLayout.NORTH);
      add(taskNotesTextArea, BorderLayout.CENTER);
      add(buttonPanel, BorderLayout.SOUTH);

      pack();
      setVisible(true);
    }

    private void handleException(Exception e, Task task) {
      JOptionPane.showMessageDialog(this, "An error occurred while trying to update your \"" + task.getTitle() +
          "\" task.", DIALOG_TITLE, JOptionPane.ERROR_MESSAGE);
    }
  }
}