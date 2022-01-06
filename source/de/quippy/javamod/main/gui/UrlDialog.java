/*
 * EditPlaylistEntry.
 *
 * JavaZOOM : jlgui@javazoom.net
 *            http://www.javazoom.net
 *
 *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */
package de.quippy.javamod.main.gui;

import javax.swing.JDialog;
import javax.swing.JFrame;

import de.quippy.javamod.system.Helpers;

/**
 * EditPlaylistEntry class implements a DialogBox to get an URL.
 */
public class UrlDialog extends JDialog
{
 	private static final long serialVersionUID = 6551932234216134125L;

 	private javax.swing.JButton cancelButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton openButton;
    private javax.swing.JTextField textField;

	private String url = null;

    /**
     * Creates new form ud
     */
    public UrlDialog(JFrame parent, boolean modal, String url)
    {
        super(parent, "Select URL", modal);
		setIconImages(parent.getIconImages());
        this.url = url;
        initialize();
    }

    /**
     * Returns URL.
     */
    public String getURL()
    {
        return url;
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     */
    private void initialize()
    {
        java.awt.GridBagConstraints gridBagConstraints;
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jLabel1 = new javax.swing.JLabel();
        jLabel1.setText("Enter an URL location to open here:");
        jLabel1.setFont(Helpers.getDialogFont());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jLabel1, gridBagConstraints);
        
        jLabel2 = new javax.swing.JLabel();
        jLabel2.setText("\"For example: http://www.server.com:8000\"");
        jLabel2.setFont(Helpers.getDialogFont());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jLabel2, gridBagConstraints);

        textField = new javax.swing.JTextField();
        textField.setColumns(10);
        textField.setFont(Helpers.getDialogFont());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(textField, gridBagConstraints);

        jPanel1 = new javax.swing.JPanel();

        openButton = new javax.swing.JButton();
        openButton.setMnemonic('O');
        openButton.setText("Open");
        openButton.setFont(Helpers.getDialogFont());
        openButton.setToolTipText("Open");
        openButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                doOpen();
            }
        });
        jPanel1.add(openButton);
        cancelButton = new javax.swing.JButton();
        cancelButton.setMnemonic('C');
        cancelButton.setText("Cancel");
        cancelButton.setToolTipText("Cancel");
        cancelButton.setFont(Helpers.getDialogFont());
        cancelButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                doCancel();
            }
        });
        jPanel1.add(cancelButton);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jPanel1, gridBagConstraints);

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter()
		{
			@Override
			public void windowClosing(java.awt.event.WindowEvent e)
			{
				doClose();
			}
		});

		setName("Select URL");
		setResizable(false);
		pack();
        if (url != null) textField.setText(url);
		setLocation(Helpers.getFrameCenteredLocation(this, getParent()));
    }

	/* EVENT METHODS -------------------------------------------------------- */
	public void doClose()
	{
		setVisible(false);
		dispose();
		//if we are alone in the world, exit the vm
		if (getParent() == null) System.exit(0); // this should not be needed! 
	}
    private void doOpen()
    {
        url = textField.getText();
        doClose();
    }
    private void doCancel()
    {
        url = null;
        doClose();
    }
}
