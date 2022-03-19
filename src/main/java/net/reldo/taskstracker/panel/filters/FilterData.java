package net.reldo.taskstracker.panel.filters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class FilterData
{
    private Map<String, List<String>> data;

    public FilterData()
    {
        data = new HashMap<>();
    }

    public List<String> getFilterValues(String keyword)
    {
        return data.get(keyword);
    }

    public void put(String keyword, List<String> values)
    {
        data.put(keyword, values);
    }
}
