import os
import matplotlib.pyplot as plt
import numpy as np
import math


def filter_x_order(data, order=2):
    new_data = []
    for i, value in enumerate(data):
        val = 0
        if i <= (order - 1):
            val += data[i+order]
        else:
            val += data[i-order]
        val -= 2*value
        if i >= len(data) - order:
            val += data[i-order]
        else:
            val += data[i+order]
        new_data.append(val)
    return new_data


def get_hr_time(data):
    time = 0
    i = 0
    time_data = []
    add_time = 0
    while i < len(data):
        if data[i]:
            if time:
                time_data.append(time + add_time)
                time = 0
                add_time = 0
            else:
                add_time += 1
        else:
            time += 1
        i += 1
    return time_data


def compact_hr_times(data, cutoff, mean):
    new_data = []
    added = 0
    last_val = 0
    median = np.median(data)
    for i, value in enumerate(data):
        if value < cutoff and i > 0:
            added += value
            if median > last_val > 0:
                new_data[len(new_data) - 1] += added
                last_val += added
                added = 0
            if added > mean:
                last_val = value+added
                new_data.append(last_val)
                added = 0
        else:
            last_val = value+added
            new_data.append(last_val)
            added = 0
    if added and len(new_data):
        new_data[len(new_data) - 1] += added
    return new_data


def split_hr_times(data, c):
    mean = np.mean(data)
    std = np.std(data)
    median = np.median(data)
    cutoff = mean + c * std
    if mean < median:
        cutoff = median + c * std
    new_data = []
    for i in data:
        if i > cutoff:
            avg = int(round(i / median))
            val = i / avg
            for j in range(avg):
                new_data.append(val)
        else:
            new_data.append(i)
    return new_data


def filter_hr_times(data, c):
    compact = compact_hr_times(data, 24, np.mean(data))# this would equal a HR of 250...... :S (60 / 0.24 @ 100hz sampling)
    return split_hr_times(compact, c)


def get_peaks(data):
    found_peaks = []
    for time in data:
        i = 0
        while i < time:
            found_peaks.append(0)
            i += 1
        found_peaks.append(3000)
    return found_peaks


def process(raw_ecg_data):
    filter_order = 3
    ecg_cutoff = 2000000
    sample_freq = 100
    c = 1.5
    filtered_data = filter_x_order(raw_ecg_data, filter_order)
    neg_data = [-f if f < 0 else 0 for f in filtered_data]
    cutoff_data = [f if f**2 > ecg_cutoff else 0 for f in neg_data]
    time_data = get_hr_time(cutoff_data)
    hr_data = filter_hr_times(time_data, c)
    return [60 / (i / sample_freq) for i in hr_data], get_peaks(hr_data)


def read_file(filename):
    raw_ecg_data = []
    with open(filename, "r") as file:
        for line in file:
            line = line.strip()
            if len(line):
                raw_ecg_data.append(int(line))
    return raw_ecg_data


if __name__ == '__main__':
    plot_length = 6000
    files = [f for f in os.listdir('.') if os.path.isfile(f) and f.endswith(".txt")]
    for filename in files:
        plot_start = 0
        raw_ecg_data = read_file(filename)
        num_plots = math.floor(len(raw_ecg_data) / plot_length)
        figure_name = filename.replace("sensorData", "").replace(".txt", ".png").lower()
        ecg_data = raw_ecg_data[plot_start:plot_start+plot_length]
        hr, peaks = process(ecg_data)
        hr_y = []
        j = 0
        for i, value in enumerate(peaks):
            if value > 0:
                hr_y.append(i)
        fig, ax1 = plt.subplots()
        fig.suptitle(figure_name)
        ecg_line = ax1.plot(ecg_data, 'o-', color="blue", label="ECG")
        ax1.set_ylabel("Raw ECG Value")
        ax2 = ax1.twinx()
        hr_line = ax2.plot(hr_y, hr, 'o-', color="red", label="Heart Rate")
        lines = ecg_line + hr_line
        labs = [l.get_label() for l in lines]
        ax1.legend(lines, labs, loc=0)
        ax2.set_ylabel("Heart Rate")
        fig.set_size_inches(20, 6)
        fig.savefig(figure_name, dpi=100)
        plt.close()