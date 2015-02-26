package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2015      *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/*
 * This class is used for creating Time Series 
 * Plot. This class is also extended to get time
 * series prediction
 *
 */
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import net.sourceforge.openforecast.DataPoint;
import net.sourceforge.openforecast.DataSet;
import net.sourceforge.openforecast.ForecastingModel;
import net.sourceforge.openforecast.Observation;
import net.sourceforge.openforecast.models.MovingAverageModel;
import net.sourceforge.openforecast.models.PolynomialRegressionModel;
import net.sourceforge.openforecast.models.RegressionModel;

import org.arrah.framework.datagen.TimeUtil;
import org.arrah.framework.ndtable.ReportTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Quarter;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.time.Week;
import org.jfree.data.time.Year;


public class TSPlotPanel extends JPanel implements ActionListener, Serializable {
	private static final long serialVersionUID = 1L;
	private String xTitle, yTitle;
	private String title;
	private TimeSeries dataset;

	public TSPlotPanel(String titleName, String xName, String yName) {
		title = titleName;
		xTitle = xName;
		yTitle = yName;
		dataset = new TimeSeries("Time Series Plot");
		addMouseListener(new PopupListener());
	}
	
	public void addRTMDataSet(ReportTableModel rtm, String datecol1, String numcol2, int timed) throws Exception {
		int rowC= rtm.getModel().getRowCount();		
		int index = rtm.getColumnIndex(datecol1);
		int comIndex = rtm.getColumnIndex(numcol2);
		
		for (int i=0; i < rowC; i++) {
			try {
				Object dcell = rtm.getModel().getValueAt(i, index);
				Object ncell = rtm.getModel().getValueAt(i, comIndex);
				switch(timed) {
					case 0: //year
						dataset.addOrUpdate(new Year((Date)dcell) , (Double)ncell);
						break;
					case 1: //Quarter
						dataset.addOrUpdate(new Quarter((Date)dcell) , (Double)ncell);
						break;
					case 2: //Month
						dataset.addOrUpdate(new Month((Date)dcell) , (Double)ncell);
						break;
					case 3: //Week
						dataset.addOrUpdate(new Week((Date)dcell) , (Double)ncell);
						break;
					case 4: //Day
						dataset.addOrUpdate(new Day((Date)dcell) , (Double)ncell);
						break;
					case 5: //Hour
						dataset.addOrUpdate(new Hour((Date)dcell) , (Double)ncell);
						break;
					case 6: //Minute
						dataset.addOrUpdate(new Minute((Date)dcell) , (Double)ncell);
						break;
					case 7: //Second
						dataset.addOrUpdate(new Second((Date)dcell) , (Double)ncell);
						break;
					case 8: //Millisecond
						dataset.addOrUpdate(new Millisecond((Date)dcell) , (Double)ncell);
						break;
						
					default:
				}
			} catch (Exception e) {
				ConsoleFrame.addText("\n Exception for row :" +i + "  Execption:"+e.getLocalizedMessage());
			}
		}
	}
	// Create the Time Series Plot
	public void drawTSPlot() throws Exception {
		TimeSeriesCollection tsdataset = new TimeSeriesCollection();
		tsdataset.addSeries(dataset);
		
	JFreeChart chart = ChartFactory.createTimeSeriesChart(
				title,  // title
				xTitle,             // x-axis label
				yTitle,   // y-axis label
				tsdataset,            // data
				true,               // create legend?
				true,               // generate tooltips?
				false               // generate URLs?
	);
	XYPlot plot = (XYPlot) chart.getPlot();
	DateAxis axis = (DateAxis) plot.getDomainAxis();
	axis.setAutoRange(true);
    final ChartPanel chartPanel = new ChartPanel(chart);
    chartPanel.setPreferredSize(new java.awt.Dimension(700, 600));
    this.setLayout(new BorderLayout());
    this.add(chartPanel,BorderLayout.CENTER);
	}
	
	private class PopupListener extends MouseAdapter {
		PopupListener() {
		}

		public void mousePressed(final MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(final MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				JPopupMenu popup = new JPopupMenu();

				JMenuItem menuItem = new JMenuItem("Save as Image");
				menuItem.setActionCommand("saveimage");
				menuItem.addActionListener(TSPlotPanel.this);
				popup.add(menuItem);
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		// Save Image
		ImageUtil imgutil = new ImageUtil(this, "png");
		imgutil.removeWaring();
		return;
	}
	
	// code from Forecast open source
	 private TimeSeries getForecastTimeSeries( ForecastingModel model,
             DataSet initDataSet, TimeSeries fc, String title )
	 {
		 // Initialize the forecasting model
		 model.init( initDataSet );
		 // Get range of data required for forecast
		 DataSet fcDataSet = getDataSet( fc );

		 // Obtain forecast values for the forecast data set
		 model.forecast( fcDataSet );

		 // Create a new TimeSeries
		 TimeSeries series
		 	= new TimeSeries(title);

		 // Iterator through the forecast results, adding to the series
		 Iterator<DataPoint> it = fcDataSet.iterator();
		 while ( it.hasNext() )
		 {
			 DataPoint dp = (DataPoint)it.next();
			 int index = (int)dp.getIndependentValue("t");
			 series.add( fc.getTimePeriod(index), dp.getDependentValue() );
		 }

		 return series;
	 }
	 
	 private DataSet getDataSet( TimeSeries series) {
		 DataSet dataSet = new DataSet();
		 if (series.isEmpty() == true) return dataSet;
		 
		 int endIndex = series.getItemCount(); 
		 
		 for ( int i=0; i<endIndex; i++ )
		 {
			 TimeSeriesDataItem dataPair = series.getDataItem(i);
			 DataPoint dp = new Observation( dataPair.getValue().doubleValue() );
			 dp.setIndependentValue( "t", i );
			 dataSet.add( dp );
		 }

		 return dataSet;
	 }

	 
	// Create the Time Series Forecasting plot
		public void drawTSForecastPlot() throws Exception {
		
			DataSet initDataSet = getDataSet( dataset);
			TimeSeries fc = TimeUtil.getForecastData(dataset, 10); // forecast hardcoded to 10%
			int diff = fc.getItemCount() - dataset.getItemCount();
			TimeSeries ma8Series = null,regressionSeries= null,polyRegressSeries= null;
			
			if (diff > 0 ) { // forecast only if positive data
	        
	         ma8Series
	            = getForecastTimeSeries(new MovingAverageModel(diff),
	                                    initDataSet, fc,"10% Period Moving Average");
	         regressionSeries
	            = getForecastTimeSeries(new RegressionModel("t"),
	                                    initDataSet, fc,"Linear regression");
	         polyRegressSeries
	            = getForecastTimeSeries(new PolynomialRegressionModel("t",4),
	                                    initDataSet,fc, "4th Polynomial regression");
			}
			
		TimeSeriesCollection tsdataset = new TimeSeriesCollection();
		tsdataset.addSeries(dataset);
		tsdataset.addSeries(ma8Series);
		tsdataset.addSeries(regressionSeries);
		tsdataset.addSeries(polyRegressSeries);
			
		JFreeChart chart = ChartFactory.createTimeSeriesChart(
					title,  // title
					xTitle,             // x-axis label
					yTitle,   // y-axis label
					tsdataset,            // data
					true,               // create legend?
					true,               // generate tooltips?
					false               // generate URLs?
		);
		XYPlot plot = (XYPlot) chart.getPlot();
		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setAutoRange(true);
	    final ChartPanel chartPanel = new ChartPanel(chart);
	    chartPanel.setPreferredSize(new java.awt.Dimension(700, 600));
	    this.setLayout(new BorderLayout());
	    this.add(chartPanel,BorderLayout.CENTER);
		}


}
