from datetime import datetime

import pyglow

terminate = 0
while not terminate:
    year = input()
    month = input()
    day = input()
    hour = input()
    minute = input()
    latitude = input()
    longitude = input()
    altitude = input()
    terminate = int(input())

    time = datetime(int(year), int(month), int(day), int(hour), int(minute))

    pt = pyglow.Point(time, float(latitude), float(longitude), float(altitude))
    pt.run_iri() 

    print(pt.ne, flush=True)

    if terminate:
        break

