"""
Every photo the game can show. One entry per image name used in the app (see the Kotlin content tables:
domain/content/*.kt and assets/rich_list.json). Used by find_photos.py, process_photos.py and to write ART_LIST.md.

kind:  "card"   4:3, 720 px wide   (businesses, properties, luxury items, stock companies)
       "person" 1:1, 400 px        (real people on the Rich List; Wikimedia Commons only)
query: search words for Wikimedia Commons (find_photos.py)
"""
import json
import os
import re
import unicodedata

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))
RICH_LIST = os.path.join(ROOT, "app", "src", "main", "assets", "rich_list.json")

# (image name, what it should show, Commons search words)
BUSINESSES = [
    ("biz_lemonade_stand_1", "Small street lemonade stand", "lemonade stand"),
    ("biz_lemonade_stand_2", "Busy juice bar counter", "juice bar"),
    ("biz_lemonade_stand_3", "Juice or bottling factory line", "juice bottling plant"),
    ("biz_food_truck_1", "A single food truck", "food truck"),
    ("biz_food_truck_2", "Several food trucks at a street food event", "food trucks festival"),
    ("biz_food_truck_3", "Busy street food market hall", "food hall market interior"),
    ("biz_coffee_shop_1", "Small café counter with espresso machine", "espresso machine"),
    ("biz_coffee_shop_2", "Busy coffee shop interior", "coffee shop interior"),
    ("biz_coffee_shop_3", "Coffee roastery with roasting machine", "coffee roaster roastery"),
    ("biz_car_wash_1", "Car being hand-washed with foam", "car wash foam"),
    ("biz_car_wash_2", "Automatic car wash tunnel with brushes", "car wash brushes"),
    ("biz_car_wash_3", "Large car wash station with several bays", "car wash station"),
    ("biz_bakery_chain_1", "Bread and pastries in a small bakery display", "bakery display bread"),
    ("biz_bakery_chain_2", "Busy bakery shop front", "bakery shop"),
    ("biz_bakery_chain_3", "Industrial bakery production line", "industrial bakery"),
    ("biz_fitness_club_1", "Small gym room with weights", "gym dumbbells"),
    ("biz_fitness_club_2", "Busy gym floor with machines", "fitness center"),
    ("biz_fitness_club_3", "Large modern fitness centre", "fitness club interior"),
    ("biz_tech_startup_1", "Laptop on a desk in a small office", "laptop desk office"),
    ("biz_tech_startup_2", "Open-plan tech office", "coworking space"),
    ("biz_tech_startup_3", "Modern glass corporate campus building", "modern office building glass"),
    ("biz_boutique_hotel_1", "Small boutique hotel facade", "small hotel facade"),
    ("biz_boutique_hotel_2", "Hotel with swimming pool", "hotel pool"),
    ("biz_boutique_hotel_3", "Grand luxury hotel tower", "luxury hotel"),
    ("biz_shipping_company_1", "Small cargo boat", "cargo boat"),
    ("biz_shipping_company_2", "Container ship at sea", "container ship"),
    ("biz_shipping_company_3", "Container port with cranes", "container terminal cranes"),
    ("biz_regional_airline_1", "Small propeller passenger plane", "turboprop aircraft"),
    ("biz_regional_airline_2", "Regional jet at an airport gate", "jet bridge"),
    ("biz_regional_airline_3", "Busy airport apron with many planes", "airport apron"),
    ("biz_energy_plant_1", "Solar panels on a roof", "rooftop solar panels"),
    ("biz_energy_plant_2", "Wind farm", "wind farm"),
    ("biz_energy_plant_3", "Large solar power station", "solar power station"),
    ("biz_orbital_launch_1", "Small rocket on a launch pad", "sounding rocket"),
    ("biz_orbital_launch_2", "Large rocket on the launch pad", "Saturn V launch pad"),
    ("biz_orbital_launch_3", "Rocket lifting off with flames", "rocket launch liftoff"),
]

PROPERTIES = [
    ("prop_studio_flat", "Small apartment building / studio flat", "small apartment building facade"),
    ("prop_corner_shop", "Corner shop on a street", "corner shop"),
    ("prop_suburban_house", "Suburban family house with garden", "suburban house"),
    ("prop_city_duplex", "Two joined townhouses", "townhouses"),
    ("prop_downtown_loft", "Brick loft building downtown", "loft building brick"),
    ("prop_retail_strip", "Row of shops with parking", "strip mall"),
    ("prop_apartment_block", "Mid-rise apartment block", "apartment block"),
    ("prop_beachfront_villa", "White villa on the beach", "beach villa"),
    ("prop_business_park", "Business park office buildings", "business park"),
    ("prop_office_tower", "Tall glass office tower", "skyscraper glass"),
    ("prop_harbor_marina", "Marina with sailboats", "marina sailboats"),
    ("prop_private_island_resort", "Island resort with overwater bungalows", "overwater bungalows"),
]

LUXURY = [
    ("lux_aurora_roadster", "Red convertible sports car (no badges)", "red convertible sports car"),
    ("lux_strata_gt", "Blue grand tourer coupe (no badges)", "grand tourer coupe"),
    ("lux_halberd_hypercar", "Low supercar (no badges)", "supercar"),
    ("lux_meridian_classic", "Vintage classic car (no badges)", "vintage classic car"),
    ("lux_lakeside_cabin", "Log cabin by a lake", "log cabin"),
    ("lux_hilltop_mansion", "Mansion on a hill", "mansion"),
    ("lux_chateau_belmont", "French château", "chateau"),
    ("lux_sky_garden_estate", "Modern villa with terraces and gardens", "modern villa terrace"),
    ("lux_day_cruiser", "Small motorboat / speedboat", "speedboat"),
    ("lux_coral_mist_yacht", "White motor yacht", "motor yacht"),
    ("lux_leviathan_superyacht", "Superyacht", "superyacht"),
    ("lux_swift_light_jet", "Small business jet", "business jet"),
    ("lux_stratos_long_range", "Large long-range business jet", "business jet long range"),
    ("lux_private_airliner", "Private wide-body airliner", "VIP airliner"),
    ("lux_palm_cay_island", "Small tropical island with palm trees", "tropical island palm"),
    ("lux_coral_atoll", "Coral atoll from the air", "atoll aerial"),
    ("lux_abstract_in_teal", "Abstract painting in a gallery (public-domain artwork)", "abstract painting"),
    ("lux_marble_muse", "Marble sculpture", "marble sculpture"),
    ("lux_lost_harbor_masterpiece", "Old master seascape painting (public domain)", "ships in harbour painting"),
    ("lux_ancient_star_map", "Antique celestial map (public domain)", "celestial map antique"),
    ("lux_racehorse_stable", "Horse stable or racehorse", "racehorse stable"),
    ("lux_lunar_retreat", "Lunar base concept art (public domain, e.g. NASA)", "lunar outpost concept"),
]

STOCKS = [
    ("stock_nimbus_cloud", "Data centre server racks", "datacenter racks"),
    ("stock_quantis_labs", "Quantum computer or high-tech lab", "optical table laser"),
    ("stock_solace_energy", "Solar farm", "solar farm"),
    ("stock_ridgeway_power", "Power lines and pylons", "power lines pylons"),
    ("stock_verdant_health", "Hospital or clinic", "hospital building"),
    ("stock_brightfen_bio", "Biotech lab with pipettes", "laboratory pipette"),
    ("stock_ironbridge_capital", "Financial district skyline", "financial district"),
    ("stock_maple_finch", "Fresh food on market shelves", "vegetable market stall"),
    ("stock_pellucid_apparel", "Clothing rack in a boutique", "clothing rack boutique"),
    ("stock_cobalt_rail", "Modern passenger train", "high speed train"),
]


def photo_name(name):
    """Same rule as RichListEngine.photoName in Kotlin: "Carlos Slim Helú" -> "person_carlos_slim_helu"."""
    ascii_ = unicodedata.normalize("NFD", name)
    ascii_ = "".join(ch for ch in ascii_ if unicodedata.category(ch) != "Mn").lower()
    return "person_" + re.sub(r"[^a-z0-9]+", "_", ascii_).strip("_")


def people():
    with open(RICH_LIST, encoding="utf-8") as fh:
        data = json.load(fh)
    return [(photo_name(p["name"]), p["name"], p["description"]) for p in data["people"]]


def all_entries():
    """List of dicts: imageName, kind, shows, query (+ person name for people)."""
    out = []
    for group, rows in (("Businesses (3 stages: _1 start, _2 25+ owned, _3 100+ owned)", BUSINESSES),
                        ("Properties", PROPERTIES), ("Luxury items", LUXURY), ("Stock companies", STOCKS)):
        for name, shows, query in rows:
            out.append(dict(imageName=name, kind="card", group=group, shows=shows, query=query))
    for name, person, desc in people():
        out.append(dict(imageName=name, kind="person", group="Rich List people (Wikimedia Commons only)",
                        shows="%s (%s)" % (person, desc), query=person, person=person))
    return out
