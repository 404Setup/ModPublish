#!/usr/bin/env python3
import os
from xml.etree import ElementTree as ET

FRAMES = 12
CENTER_X, CENTER_Y = 12, 12

with open("base.svg", "r", encoding="utf-8") as f:
    svg = ET.parse(f)

root = svg.getroot()

g = root.find("{http://www.w3.org/2000/svg}g")

for i in range(FRAMES):
    angle = i * (360 / FRAMES)

    g.set("transform", f"rotate({angle} {CENTER_X} {CENTER_Y})")

    out_name = f"frame_{i}.svg"
    svg.write(out_name, encoding="utf-8", xml_declaration=True)
    print("Generated", out_name)