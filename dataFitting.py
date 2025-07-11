import pandas as pd
from fitter import Fitter
import matplotlib.pyplot as plt

# Load your CSV and filter out missing values
df = pd.read_csv('EVChargingStationUsage.csv')
df["Start Date"] = pd.to_datetime(df["Start Date"])
eDF = df.dropna(subset=['Energy (kWh)'])

def dateTimeToSeconds(t):
    return t.hour * 3600 + t.minute * 60 + t.second

# Get the energy data as a NumPy array
energy_data = eDF['Energy (kWh)'].values
time_data = df["Start Date"].dt.time.apply(dateTimeToSeconds)


# Use Fitter to fit multiple distributions
f = Fitter(energy_data,
           distributions=[
               'gamma', 'lognorm', 'beta', 'expon', 'norm', 'weibull_min', 'weibull_max'
           ])
f.fit()

# Show top 5 best fits
f.summary()

print()

f = Fitter(time_data, 
            distributions=[
               'gamma', 'lognorm', 'beta', 'expon', 'norm', 'weibull_min', 'weibull_max'
           ])
f.fit()
f.summary()