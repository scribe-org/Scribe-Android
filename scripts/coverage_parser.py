import xml.etree.ElementTree as ET
from collections import defaultdict
import os

def parse_kover_coverage_report(report_path):
    if not os.path.exists(report_path):
        print(f"Report not found at: {report_path}")
        return

    tree = ET.parse(report_path)
    root = tree.getroot()

    coverage_by_class = defaultdict(lambda: defaultdict(int))

    for class_elem in root.findall(".//class"):
        class_name = class_elem.get("name", "Unknown").replace("/", ".")
        source_file = class_elem.get("sourcefilename", "Unknown")

        key = f"{class_name} (Source: {source_file})"

        for counter in class_elem.findall("counter"):
            ctype = counter.get("type")
            covered = int(counter.get("covered", 0))
            missed = int(counter.get("missed", 0))

            coverage_by_class[key][ctype] = {
                "covered": covered,
                "missed": missed,
                "total": covered + missed,
                "percentage": (covered / (covered + missed) * 100) if (covered + missed) else 0
            }

    print("File-wise Coverage Summary")
    print("------------------------------------------------------------")
    for class_key, counters in coverage_by_class.items():
        print(f"\n {class_key}")
        for ctype in ["INSTRUCTION", "BRANCH", "LINE", "METHOD"]:
            if ctype in counters:
                data = counters[ctype]
                print(f"  {ctype:<12}: {data['covered']}/{data['total']} ({data['percentage']:.2f}%)")
    print("Done.")

if __name__ == "__main__":
    REPORT_PATH = "../app/build/reports/kover/xml/report.xml"
    parse_kover_coverage_report(REPORT_PATH)
