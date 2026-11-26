import matplotlib.pyplot as plt
import numpy as np

q_labels = ['1', '2', '3', '4', '6', '8', '40']
x = np.arange(len(q_labels))

data = {
    "Test Case 1": {
        "wt":  {0: [2.67, 3.33, 4.33, 3.33, 4.00, 3.67, 3.67],
                1: [7.00, 6.33, 7.33, 5.00, 5.67, 4.67, 4.67],
                2: [12.67, 9.33, 10.33, 6.67, 7.33, 5.67, 5.67]},
        "tat": {0: [7.00, 7.67, 8.67, 7.67, 8.33, 8.00, 8.00],
                1: [11.33, 10.67, 11.67, 9.33, 10.00, 9.00, 9.00],
                2: [17.00, 13.67, 14.67, 11.00, 11.67, 10.00, 10.00]},
    },
    "Test Case 2": {
        "wt":  {0: [4.67, 6.00, 7.00, 7.00, 3.67, 3.67, 3.67],
                1: [7.33, 10.00, 11.00, 10.00, 4.67, 4.67, 4.67],
                2: [13.00, 14.00, 15.00, 12.67, 5.67, 5.67, 5.67]},
        "tat": {0: [9.67, 11.00, 12.00, 12.00, 8.67, 8.67, 8.67],
                1: [12.33, 15.00, 16.00, 15.00, 9.67, 9.67, 9.67],
                2: [18.00, 19.00, 20.00, 17.67, 10.67, 10.67, 10.67]},
    },
    "Test Case 3": {
        "wt":  {0: [18.50, 10.00, 8.33, 9.50, 9.83, 12.50, 20.00],
                1: [22.33, 11.67, 10.33, 11.67, 14.33, 15.17, 22.50],
                2: [18.17, 13.50, 14.83, 16.17, 16.67, 19.67, 25.00]},
        "tat": {0: [25.33, 16.83, 15.17, 16.33, 16.67, 19.33, 26.83],
                1: [29.17, 18.50, 17.17, 18.50, 21.17, 22.00, 29.33],
                2: [25.00, 20.33, 21.67, 23.00, 23.50, 26.50, 31.83]},
    },
}

styles = {
    0: ("CS = 0", "o-", "#1f77b4"),
    1: ("CS = 1", "s--", "#ff7f0e"),
    2: ("CS = 2", "^:", "#2ca02c"),
}

for idx, (tc_name, metrics) in enumerate(data.items(), start=1):
    fig, axes = plt.subplots(1, 2, figsize=(11, 4.2), dpi=300)
    for ax, key, title in zip(axes, ["wt", "tat"], ["Average Waiting Time", "Average Turnaround Time"]):
        for cs, (label, style, color) in styles.items():
            ax.plot(x, metrics[key][cs], style, label=label, color=color, linewidth=2, markersize=6)
        ax.set_title(f"{tc_name} — {title}", fontsize=11, fontweight="bold")
        ax.set_xlabel("Quantum (Q)")
        ax.set_ylabel("Time Units")
        ax.set_xticks(x)
        ax.set_xticklabels(q_labels)
        ax.grid(True, linestyle="--", alpha=0.4)
        ax.legend()
    plt.tight_layout()
    plt.savefig(f"graph_tc{idx}.png")
    plt.close()

# Graph 4: Trade-off ACS vs Round Robin (1000 workloads, CS=1)
util_acs = [78.9, 80.1, 81.2, 81.1, 81.0, 80.8, 83.6]
util_rr  = [51.7, 64.7, 70.9, 73.9, 77.1, 78.8, 83.6]
resp_acs = [8.34, 8.32, 8.28, 7.90, 7.52, 7.99, 21.17]
resp_rr  = [3.73, 4.84, 5.92, 7.03, 9.03, 10.85, 21.17]

fig, axes = plt.subplots(1, 2, figsize=(11, 4.2), dpi=300)
axes[0].plot(x, util_acs, "o-", label="ACS", linewidth=2)
axes[0].plot(x, util_rr, "s--", label="Plain RR", linewidth=2)
axes[0].set_title("CPU Utilization (%) — ACS vs RR (CS=1)", fontsize=11, fontweight="bold")
axes[0].set_xlabel("Quantum (Q)")
axes[0].set_ylabel("Utilization (%)")
axes[0].set_xticks(x); axes[0].set_xticklabels(q_labels); axes[0].grid(True, alpha=0.4); axes[0].legend()

axes[1].plot(x, resp_acs, "o-", label="ACS", linewidth=2)
axes[1].plot(x, resp_rr, "s--", label="Plain RR", linewidth=2)
axes[1].set_title("Average Response Time — ACS vs RR (CS=1)", fontsize=11, fontweight="bold")
axes[1].set_xlabel("Quantum (Q)")
axes[1].set_ylabel("Time Units")
axes[1].set_xticks(x); axes[1].set_xticklabels(q_labels); axes[1].grid(True, alpha=0.4); axes[1].legend()

plt.tight_layout()
plt.savefig("graph_tradeoff.png")
plt.close()
print("Generated: graph_tc1.png, graph_tc2.png, graph_tc3.png, graph_tradeoff.png")