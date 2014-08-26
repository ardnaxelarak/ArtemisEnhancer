import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class DispLauncher extends Frame
{
	private Display disp;
	public DispLauncher(String host, int port)
	{
		super("Game Window");
		setLayout(new BorderLayout());
		disp = new Display(host, port);
		disp.setSize(new Dimension(1200, 800));
		add(disp, BorderLayout.CENTER);
		disp.init();
		pack();
		setVisible(true);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				disp.exit();
			}
		});
	}
}
