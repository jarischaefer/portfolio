package name.abuchen.portfolio.ui.views.dashboard;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Display;

import name.abuchen.portfolio.model.Dashboard;
import name.abuchen.portfolio.ui.Messages;
import name.abuchen.portfolio.ui.dialogs.ListSelectionDialog;
import name.abuchen.portfolio.ui.util.LabelOnly;
import name.abuchen.portfolio.ui.util.SimpleAction;
import name.abuchen.portfolio.ui.views.dataseries.DataSeries;
import name.abuchen.portfolio.ui.views.dataseries.DataSeriesLabelProvider;

public class DataSeriesConfig implements WidgetConfig
{
    private final WidgetDelegate delegate;
    private final boolean supportsBenchmarks;

    private DataSeries dataSeries;

    public DataSeriesConfig(WidgetDelegate delegate, boolean supportsBenchmarks)
    {
        this.delegate = delegate;
        this.supportsBenchmarks = supportsBenchmarks;

        String uuid = delegate.getWidget().getConfiguration().get(Dashboard.Config.DATA_SERIES.name());
        if (uuid != null && !uuid.isEmpty())
            dataSeries = delegate.getDashboardData().getDataSeriesSet().lookup(uuid);
        if (dataSeries == null)
            dataSeries = delegate.getDashboardData().getDataSeriesSet().getAvailableSeries().stream()
                            .filter(ds -> ds.getType().equals(DataSeries.Type.CLIENT)).findAny().get();
    }

    public DataSeries getDataSeries()
    {
        return dataSeries;
    }

    @Override
    public void menuAboutToShow(IMenuManager manager)
    {
        manager.appendToGroup(DashboardView.INFO_MENU_GROUP_NAME, new LabelOnly(dataSeries.getLabel()));

        MenuManager subMenu = new MenuManager(Messages.LabelDataSeries);
        subMenu.add(new LabelOnly(dataSeries.getLabel()));
        subMenu.add(new Separator());
        subMenu.add(new SimpleAction(Messages.MenuSelectDataSeries, a -> doAddSeries(false)));

        if (supportsBenchmarks)
            subMenu.add(new SimpleAction(Messages.MenuSelectBenchmarkDataSeries, a -> doAddSeries(true)));

        manager.add(subMenu);
    }

    private void doAddSeries(boolean showOnlyBenchmark)
    {
        List<DataSeries> list = delegate.getDashboardData().getDataSeriesSet().getAvailableSeries().stream()
                        .filter(ds -> ds.isBenchmark() == showOnlyBenchmark).collect(Collectors.toList());

        ListSelectionDialog dialog = new ListSelectionDialog(Display.getDefault().getActiveShell(),
                        new DataSeriesLabelProvider());
        dialog.setTitle(Messages.ChartSeriesPickerTitle);
        dialog.setMessage(Messages.ChartSeriesPickerTitle);
        dialog.setElements(list);
        dialog.setMultiSelection(false);

        if (dialog.open() != ListSelectionDialog.OK)
            return;

        Object[] result = dialog.getResult();
        if (result == null || result.length == 0)
            return;

        dataSeries = (DataSeries) result[0];
        delegate.getWidget().getConfiguration().put(Dashboard.Config.DATA_SERIES.name(), dataSeries.getUUID());

        // construct label to indicate the data series (user can manually change
        // the label later)
        String label = WidgetFactory.valueOf(delegate.getWidget().getType()).getLabel() + ", " + dataSeries.getLabel(); //$NON-NLS-1$
        delegate.getWidget().setLabel(label);

        delegate.getClient().markDirty();
    }


    @Override
    public String getLabel()
    {
        return Messages.LabelDataSeries + ": " + dataSeries.getLabel(); //$NON-NLS-1$
    }
}
