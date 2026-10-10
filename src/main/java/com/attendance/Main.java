package com.attendance;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class Main {
    private static final String DB_URL="jdbc:mysql://localhost:3306/attendance_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER="root";
    private static final String DB_PASSWORD="YOUR_MYSQL_PASSWORD";
    private static Connection db() throws SQLException { return DriverManager.getConnection(DB_URL,DB_USER,DB_PASSWORD); }
    public static void main(String[] args){ SwingUtilities.invokeLater(Main::login); }

    static void login(){
        JTextField u=new JTextField("admin"); JPasswordField p=new JPasswordField();
        JPanel form=new JPanel(new GridLayout(0,1,5,5)); form.add(new JLabel("Username")); form.add(u); form.add(new JLabel("Password")); form.add(p);
        if(JOptionPane.showConfirmDialog(null,form,"Attendance System Login",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        try(Connection c=db(); PreparedStatement s=c.prepareStatement("SELECT id FROM app_users WHERE username=? AND password=?")){
            s.setString(1,u.getText().trim()); s.setString(2,new String(p.getPassword()));
            try(ResultSet r=s.executeQuery()){ if(r.next()) dashboard(); else {JOptionPane.showMessageDialog(null,"Invalid login");login();} }
        }catch(SQLException e){error(e);}
    }
    static void dashboard(){
        JFrame f=new JFrame("Student Attendance Management System"); f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); f.setSize(1050,600); f.setLocationRelativeTo(null);
        JTabbedPane tabs=new JTabbedPane(); tabs.addTab("Students",students()); tabs.addTab("Mark Attendance",attendance()); tabs.addTab("Reports",reports());
        f.add(new JLabel("  Student Attendance Management System",SwingConstants.LEFT),BorderLayout.NORTH); f.add(tabs); f.setVisible(true);
    }
    static JPanel students(){
        JPanel p=new JPanel(new BorderLayout(5,5)); JTextField roll=new JTextField(8),name=new JTextField(12),dept=new JTextField(10),year=new JTextField(8);
        JPanel top=new JPanel(); top.add(new JLabel("Roll"));top.add(roll);top.add(new JLabel("Name"));top.add(name);top.add(new JLabel("Department"));top.add(dept);top.add(new JLabel("Year"));top.add(year);
        DefaultTableModel m=new DefaultTableModel(new String[]{"ID","Roll No","Name","Department","Year"},0){public boolean isCellEditable(int r,int c){return false;}};
        JTable t=new JTable(m); JButton add=new JButton("Add"),upd=new JButton("Update selected"),del=new JButton("Delete selected"),refresh=new JButton("Refresh");
        JPanel buttons=new JPanel(); for(JButton b:new JButton[]{add,upd,del,refresh})buttons.add(b);
        Runnable load=()->{m.setRowCount(0);try(Connection c=db();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT * FROM students ORDER BY roll_no")){while(r.next())m.addRow(new Object[]{r.getInt("id"),r.getString("roll_no"),r.getString("full_name"),r.getString("department"),r.getString("year_level")});}catch(SQLException e){error(e);}};
        t.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting()&&t.getSelectedRow()>=0){int x=t.convertRowIndexToModel(t.getSelectedRow());roll.setText(""+m.getValueAt(x,1));name.setText(""+m.getValueAt(x,2));dept.setText(""+m.getValueAt(x,3));year.setText(""+m.getValueAt(x,4));}});
        add.addActionListener(e->{if(!fields(roll,name,dept,year))return;try(Connection c=db();PreparedStatement s=c.prepareStatement("INSERT INTO students(roll_no,full_name,department,year_level) VALUES(?,?,?,?)")){studentParams(s,roll,name,dept,year);s.executeUpdate();load.run();clear(roll,name,dept,year);JOptionPane.showMessageDialog(p,"Student added.");}catch(SQLException ex){error(ex);}});
        upd.addActionListener(e->{int x=t.getSelectedRow();if(x<0){JOptionPane.showMessageDialog(p,"Select a student.");return;}if(!fields(roll,name,dept,year))return;int id=(int)m.getValueAt(t.convertRowIndexToModel(x),0);try(Connection c=db();PreparedStatement s=c.prepareStatement("UPDATE students SET roll_no=?,full_name=?,department=?,year_level=? WHERE id=?")){studentParams(s,roll,name,dept,year);s.setInt(5,id);s.executeUpdate();load.run();}catch(SQLException ex){error(ex);}});
        del.addActionListener(e->{int x=t.getSelectedRow();if(x<0){JOptionPane.showMessageDialog(p,"Select a student.");return;}if(JOptionPane.showConfirmDialog(p,"Delete this student and their attendance?","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;int id=(int)m.getValueAt(t.convertRowIndexToModel(x),0);try(Connection c=db();PreparedStatement s=c.prepareStatement("DELETE FROM students WHERE id=?")){s.setInt(1,id);s.executeUpdate();load.run();clear(roll,name,dept,year);}catch(SQLException ex){error(ex);}});
        refresh.addActionListener(e->load.run());p.add(top,BorderLayout.NORTH);p.add(new JScrollPane(t));p.add(buttons,BorderLayout.SOUTH);load.run();return p;
    }
    static JPanel attendance(){
        JPanel p=new JPanel(new BorderLayout());JComboBox<String> student=new JComboBox<>(),status=new JComboBox<>(new String[]{"Present","Absent"});
        JTextField subject=new JTextField("Java",12),date=new JTextField(LocalDate.now().toString(),10);JButton load=new JButton("Load Students"),save=new JButton("Save Attendance");
        JPanel form=new JPanel(new FlowLayout(FlowLayout.LEFT));form.add(new JLabel("Student"));form.add(student);form.add(new JLabel("Subject"));form.add(subject);form.add(new JLabel("Date YYYY-MM-DD"));form.add(date);form.add(status);form.add(load);form.add(save);
        Runnable fill=()->{student.removeAllItems();try(Connection c=db();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT id,roll_no,full_name FROM students ORDER BY roll_no")){while(r.next())student.addItem(r.getInt(1)+" | "+r.getString(2)+" | "+r.getString(3));}catch(SQLException e){error(e);}};
        load.addActionListener(e->fill.run());
        save.addActionListener(e->{if(student.getSelectedItem()==null||subject.getText().trim().isEmpty()){JOptionPane.showMessageDialog(p,"Select a student and enter subject.");return;}try{int id=Integer.parseInt(student.getSelectedItem().toString().split(" \\| ")[0]);Date d=Date.valueOf(date.getText().trim());String q="INSERT INTO attendance(student_id,attendance_date,subject,status) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE status=VALUES(status)";try(Connection c=db();PreparedStatement s=c.prepareStatement(q)){s.setInt(1,id);s.setDate(2,d);s.setString(3,subject.getText().trim());s.setString(4,status.getSelectedItem().toString());s.executeUpdate();}JOptionPane.showMessageDialog(p,"Attendance saved.");}catch(IllegalArgumentException ex){JOptionPane.showMessageDialog(p,"Use date format YYYY-MM-DD.");}catch(SQLException ex){error(ex);}});
        JTextArea note=new JTextArea("Steps\n1. Select a student.\n2. Enter subject and date.\n3. Choose Present or Absent.\n4. Click Save Attendance.\n\nSaving the same student/date/subject again updates its status.");note.setEditable(false);note.setFont(new Font("SansSerif",Font.PLAIN,16));note.setBorder(BorderFactory.createEmptyBorder(25,25,25,25));p.add(form,BorderLayout.NORTH);p.add(note);fill.run();return p;
    }
    static JPanel reports(){
        JPanel p=new JPanel(new BorderLayout());JTextField roll=new JTextField(10);JButton search=new JButton("Search Roll No"),all=new JButton("Show All");
        JPanel top=new JPanel();top.add(new JLabel("Roll No"));top.add(roll);top.add(search);top.add(all);
        DefaultTableModel m=new DefaultTableModel(new String[]{"Roll No","Student","Subject","Present","Total","Attendance %"},0){public boolean isCellEditable(int r,int c){return false;}};JTable t=new JTable(m);
        String sql="SELECT s.roll_no,s.full_name,a.subject,SUM(a.status='Present'),COUNT(*),ROUND(100.0*SUM(a.status='Present')/COUNT(*),2) FROM attendance a JOIN students s ON s.id=a.student_id %s GROUP BY s.roll_no,s.full_name,a.subject ORDER BY s.roll_no,a.subject";
        java.util.function.Consumer<String> run=(filter)->{m.setRowCount(0);try(Connection c=db();PreparedStatement s=c.prepareStatement(String.format(sql,filter.isEmpty()?"":"WHERE s.roll_no=?"))){if(!filter.isEmpty())s.setString(1,filter);try(ResultSet r=s.executeQuery()){while(r.next())m.addRow(new Object[]{r.getString(1),r.getString(2),r.getString(3),r.getInt(4),r.getInt(5),r.getBigDecimal(6)+"%"});}}catch(SQLException e){error(e);}};
        all.addActionListener(e->run.accept(""));search.addActionListener(e->run.accept(roll.getText().trim()));p.add(top,BorderLayout.NORTH);p.add(new JScrollPane(t));run.accept("");return p;
    }
    static boolean fields(JTextField...f){for(JTextField x:f)if(x.getText().trim().isEmpty()){JOptionPane.showMessageDialog(null,"Fill all fields.");return false;}return true;}
    static void studentParams(PreparedStatement s,JTextField r,JTextField n,JTextField d,JTextField y)throws SQLException{s.setString(1,r.getText().trim());s.setString(2,n.getText().trim());s.setString(3,d.getText().trim());s.setString(4,y.getText().trim());}
    static void clear(JTextField...f){for(JTextField x:f)x.setText("");}
    static void error(SQLException e){e.printStackTrace();JOptionPane.showMessageDialog(null,"Database error: "+e.getMessage()+"\nCheck MySQL, schema.sql and credentials.","Error",JOptionPane.ERROR_MESSAGE);}
}
