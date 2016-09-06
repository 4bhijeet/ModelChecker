package modelChecker.views.viewpart;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.zest.core.widgets.Graph;

public class ExportImage implements IRunnableWithProgress {
	private Graph graph;
	private Display display;	
	private String fileName;
	private int format;
	
	public ExportImage(Graph graph, Display display, String fileName, int format) {
		this.graph = graph;
		this.display = display;		
		this.fileName = fileName;
		this.format = format;
	}
	
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			monitor.beginTask("Saving image", IProgressMonitor.UNKNOWN);
						
			if(format == -5)	// export to SVG format
				saveSVG(monitor);
			else 
				save(monitor);
			
		} finally {
			monitor.done();
		}
	}
	
	private void save(IProgressMonitor monitor) {
		/*	Putting it in a separate UI thread since this method accesses UI elements	*/
		display.syncExec(() -> {
			Dimension size = graph.getContents().getSize();				
			Image image = new Image(null, size.width + 5, size.height + 5);
			
			GC gc = new GC(image);
			SWTGraphics swtGraphics = new SWTGraphics(gc);
			graph.getContents().paint(swtGraphics);
			
			gc.copyArea(image, 0, 0);
			gc.dispose();
			
			ImageLoader imageLoader = new ImageLoader();
			ImageData[] imageDatas = { image.getImageData() };
			imageLoader.data = imageDatas;
			try {
				imageLoader.save(fileName, format);
			} catch(Exception ex) {
				MessageDialog.openError(new Shell( Display.getCurrent() ), "Exception", "An exception occured : " + ex.getMessage());
			} finally {
				image.dispose();
			}
		});		//<---------- Java 8 lambda expression
	}

	private void saveSVG(IProgressMonitor monitor) {
		/*	Putting it in a separate UI thread since this method accesses UI elements	*/
		display.syncExec(() -> {
			IFigure rootFigure = graph.getRootLayer();
			Rectangle bounds = rootFigure.getBounds();
			
			@SuppressWarnings("restriction")
			org.eclipse.gmf.runtime.draw2d.ui.render.awt.internal.svg.export.GraphicsSVG graphicsSVG = org.eclipse.gmf.runtime.draw2d.ui.render.awt.internal.svg.export.GraphicsSVG.getInstance( bounds.getTranslated( bounds.getLocation().negate() ));
			
			graphicsSVG.translate( bounds.getLocation().negate() );
			
			rootFigure.paint(graphicsSVG);
			
			try(FileOutputStream fout = new FileOutputStream(fileName)) {			
				@SuppressWarnings("restriction")
				SVGGraphics2D svgGraphics2D = graphicsSVG.getSVGGraphics2D();
				
				try {			
					svgGraphics2D.stream(new BufferedWriter( new OutputStreamWriter(fout) ));
				} catch(SVGGraphics2DIOException ex) {
					MessageDialog.openError(new Shell( Display.getCurrent() ), "Exception", "An exception occured while writing to SVG format : " + ex.getMessage());			
				}
				
			} catch(FileNotFoundException ex) {
				MessageDialog.openError(new Shell( Display.getCurrent() ), "Exception", "The specified file is inaccessible : " + ex.getMessage());	
			} catch(IOException ex) {
				MessageDialog.openError(new Shell( Display.getCurrent() ), "Exception", "An IOException occured while closing the file : " + ex.getMessage());	
			}
		});		//<---------- Java 8 lambda expression		
	}
}
