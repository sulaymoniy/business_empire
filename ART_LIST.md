# Art list

Every picture the game can show: 145 image names, 125 of them have a photo so far. Anything without a photo
shows a coloured placeholder card with an icon, so the game always builds and runs.

## Rules for photos
- Sources: Pexels, Unsplash, Pixabay, or Wikimedia Commons files marked Public Domain, CC0, CC BY or CC BY-SA.
  Check the licence on each file page. Never Google Images, news sites, social media, agencies or makers' sites.
- No visible brand logos, badges, licence plates or recognisable faces on item photos.
- Rich List people: Wikimedia Commons only, neutral and respectful, crop/resize only, used only inside the Rich
  List screens (never the icon, splash, store screenshots, promo graphics or notifications). No licensed photo:
  the initials avatar is used.
- Every photo must have an entry in `app/src/main/assets/image_credits.json` (shown on Settings > About > Photo
  credits).

## Adding or replacing a photo
- Easiest: add an approved entry to `tools/photos/photos.json` and run `python tools/photos/process_photos.py`.
  It downloads, crops, resizes, saves WebP and updates the credits and this list.
- By hand: save a WebP/PNG named exactly like the image name (e.g. `lux_day_cruiser.webp`) at the size below into
  `app/src/main/res/drawable-nodpi/`, and add its credit to `image_credits.json`. No code changes needed.
- Cards are 4:3 at 720 px wide; people are square at 400 px. Keep the total under 15 MB.


### Businesses (3 stages: _1 start, _2 25+ owned, _3 100+ owned)

| Image name | Size | Should show | Status |
|---|---|---|---|
| `biz_lemonade_stand_1` | 720 x 540 (4:3) | Small street lemonade stand | added |
| `biz_lemonade_stand_2` | 720 x 540 (4:3) | Busy juice bar counter | added |
| `biz_lemonade_stand_3` | 720 x 540 (4:3) | Juice or bottling factory line | added |
| `biz_food_truck_1` | 720 x 540 (4:3) | A single food truck | added |
| `biz_food_truck_2` | 720 x 540 (4:3) | Several food trucks at a street food event | added |
| `biz_food_truck_3` | 720 x 540 (4:3) | Busy street food market hall | added |
| `biz_coffee_shop_1` | 720 x 540 (4:3) | Small café counter with espresso machine | added |
| `biz_coffee_shop_2` | 720 x 540 (4:3) | Busy coffee shop interior | added |
| `biz_coffee_shop_3` | 720 x 540 (4:3) | Coffee roastery with roasting machine | added |
| `biz_car_wash_1` | 720 x 540 (4:3) | Car being hand-washed with foam | added |
| `biz_car_wash_2` | 720 x 540 (4:3) | Automatic car wash tunnel with brushes | added |
| `biz_car_wash_3` | 720 x 540 (4:3) | Large car wash station with several bays | added |
| `biz_bakery_chain_1` | 720 x 540 (4:3) | Bread and pastries in a small bakery display | added |
| `biz_bakery_chain_2` | 720 x 540 (4:3) | Busy bakery shop front | added |
| `biz_bakery_chain_3` | 720 x 540 (4:3) | Industrial bakery production line | added |
| `biz_fitness_club_1` | 720 x 540 (4:3) | Small gym room with weights | added |
| `biz_fitness_club_2` | 720 x 540 (4:3) | Busy gym floor with machines | added |
| `biz_fitness_club_3` | 720 x 540 (4:3) | Large modern fitness centre | added |
| `biz_tech_startup_1` | 720 x 540 (4:3) | Laptop on a desk in a small office | added |
| `biz_tech_startup_2` | 720 x 540 (4:3) | Open-plan tech office | added |
| `biz_tech_startup_3` | 720 x 540 (4:3) | Modern glass corporate campus building | added |
| `biz_boutique_hotel_1` | 720 x 540 (4:3) | Small boutique hotel facade | added |
| `biz_boutique_hotel_2` | 720 x 540 (4:3) | Hotel with swimming pool | added |
| `biz_boutique_hotel_3` | 720 x 540 (4:3) | Grand luxury hotel tower | added |
| `biz_shipping_company_1` | 720 x 540 (4:3) | Small cargo boat | added |
| `biz_shipping_company_2` | 720 x 540 (4:3) | Container ship at sea | added |
| `biz_shipping_company_3` | 720 x 540 (4:3) | Container port with cranes | added |
| `biz_regional_airline_1` | 720 x 540 (4:3) | Small propeller passenger plane | added |
| `biz_regional_airline_2` | 720 x 540 (4:3) | Regional jet at an airport gate | added |
| `biz_regional_airline_3` | 720 x 540 (4:3) | Busy airport apron with many planes | added |
| `biz_energy_plant_1` | 720 x 540 (4:3) | Solar panels on a roof | added |
| `biz_energy_plant_2` | 720 x 540 (4:3) | Wind farm | added |
| `biz_energy_plant_3` | 720 x 540 (4:3) | Large solar power station | added |
| `biz_orbital_launch_1` | 720 x 540 (4:3) | Small rocket on a launch pad | added |
| `biz_orbital_launch_2` | 720 x 540 (4:3) | Large rocket on the launch pad | added |
| `biz_orbital_launch_3` | 720 x 540 (4:3) | Rocket lifting off with flames | added |

### Properties

| Image name | Size | Should show | Status |
|---|---|---|---|
| `prop_studio_flat` | 720 x 540 (4:3) | Small apartment building / studio flat | added |
| `prop_corner_shop` | 720 x 540 (4:3) | Corner shop on a street | added |
| `prop_suburban_house` | 720 x 540 (4:3) | Suburban family house with garden | added |
| `prop_city_duplex` | 720 x 540 (4:3) | Two joined townhouses | added |
| `prop_downtown_loft` | 720 x 540 (4:3) | Brick loft building downtown | added |
| `prop_retail_strip` | 720 x 540 (4:3) | Row of shops with parking | added |
| `prop_apartment_block` | 720 x 540 (4:3) | Mid-rise apartment block | added |
| `prop_beachfront_villa` | 720 x 540 (4:3) | White villa on the beach | added |
| `prop_business_park` | 720 x 540 (4:3) | Business park office buildings | added |
| `prop_office_tower` | 720 x 540 (4:3) | Tall glass office tower | added |
| `prop_harbor_marina` | 720 x 540 (4:3) | Marina with sailboats | added |
| `prop_private_island_resort` | 720 x 540 (4:3) | Island resort with overwater bungalows | added |

### Luxury items

| Image name | Size | Should show | Status |
|---|---|---|---|
| `lux_aurora_roadster` | 720 x 540 (4:3) | Red convertible sports car (no badges) | placeholder |
| `lux_strata_gt` | 720 x 540 (4:3) | Blue grand tourer coupe (no badges) | added |
| `lux_halberd_hypercar` | 720 x 540 (4:3) | Low supercar (no badges) | placeholder |
| `lux_meridian_classic` | 720 x 540 (4:3) | Vintage classic car (no badges) | added |
| `lux_lakeside_cabin` | 720 x 540 (4:3) | Log cabin by a lake | added |
| `lux_hilltop_mansion` | 720 x 540 (4:3) | Mansion on a hill | added |
| `lux_chateau_belmont` | 720 x 540 (4:3) | French château | added |
| `lux_sky_garden_estate` | 720 x 540 (4:3) | Modern villa with terraces and gardens | added |
| `lux_day_cruiser` | 720 x 540 (4:3) | Small motorboat / speedboat | added |
| `lux_coral_mist_yacht` | 720 x 540 (4:3) | White motor yacht | added |
| `lux_leviathan_superyacht` | 720 x 540 (4:3) | Superyacht | added |
| `lux_swift_light_jet` | 720 x 540 (4:3) | Small business jet | added |
| `lux_stratos_long_range` | 720 x 540 (4:3) | Large long-range business jet | added |
| `lux_private_airliner` | 720 x 540 (4:3) | Private wide-body airliner | added |
| `lux_palm_cay_island` | 720 x 540 (4:3) | Small tropical island with palm trees | added |
| `lux_coral_atoll` | 720 x 540 (4:3) | Coral atoll from the air | added |
| `lux_abstract_in_teal` | 720 x 540 (4:3) | Abstract painting in a gallery (public-domain artwork) | added |
| `lux_marble_muse` | 720 x 540 (4:3) | Marble sculpture | added |
| `lux_lost_harbor_masterpiece` | 720 x 540 (4:3) | Old master seascape painting (public domain) | placeholder |
| `lux_ancient_star_map` | 720 x 540 (4:3) | Antique celestial map (public domain) | added |
| `lux_racehorse_stable` | 720 x 540 (4:3) | Horse stable or racehorse | added |
| `lux_lunar_retreat` | 720 x 540 (4:3) | Lunar base concept art (public domain, e.g. NASA) | added |

### Stock companies

| Image name | Size | Should show | Status |
|---|---|---|---|
| `stock_nimbus_cloud` | 720 x 540 (4:3) | Data centre server racks | added |
| `stock_quantis_labs` | 720 x 540 (4:3) | Quantum computer or high-tech lab | added |
| `stock_solace_energy` | 720 x 540 (4:3) | Solar farm | added |
| `stock_ridgeway_power` | 720 x 540 (4:3) | Power lines and pylons | added |
| `stock_verdant_health` | 720 x 540 (4:3) | Hospital or clinic | added |
| `stock_brightfen_bio` | 720 x 540 (4:3) | Biotech lab with pipettes | added |
| `stock_ironbridge_capital` | 720 x 540 (4:3) | Financial district skyline | added |
| `stock_maple_finch` | 720 x 540 (4:3) | Fresh food on market shelves | added |
| `stock_pellucid_apparel` | 720 x 540 (4:3) | Clothing rack in a boutique | added |
| `stock_cobalt_rail` | 720 x 540 (4:3) | Modern passenger train | added |

### Rich List people (Wikimedia Commons only)

| Image name | Size | Should show | Status |
|---|---|---|---|
| `person_elon_musk` | 400 x 400 (1:1) | Elon Musk (Entrepreneur, electric cars and rockets) | added |
| `person_larry_ellison` | 400 x 400 (1:1) | Larry Ellison (Co-founder, software company) | added |
| `person_mark_zuckerberg` | 400 x 400 (1:1) | Mark Zuckerberg (Co-founder, social media company) | added |
| `person_jeff_bezos` | 400 x 400 (1:1) | Jeff Bezos (Founder, online retailer) | added |
| `person_larry_page` | 400 x 400 (1:1) | Larry Page (Co-founder, internet search company) | added |
| `person_bernard_arnault` | 400 x 400 (1:1) | Bernard Arnault (Chairman, luxury goods group) | added |
| `person_sergey_brin` | 400 x 400 (1:1) | Sergey Brin (Co-founder, internet search company) | added |
| `person_jensen_huang` | 400 x 400 (1:1) | Jensen Huang (Co-founder, chipmaker) | placeholder |
| `person_warren_buffett` | 400 x 400 (1:1) | Warren Buffett (Investor) | added |
| `person_steve_ballmer` | 400 x 400 (1:1) | Steve Ballmer (Former CEO, software company) | added |
| `person_amancio_ortega` | 400 x 400 (1:1) | Amancio Ortega (Founder, fashion retailer) | placeholder |
| `person_michael_dell` | 400 x 400 (1:1) | Michael Dell (Founder, computer maker) | added |
| `person_alice_walton` | 400 x 400 (1:1) | Alice Walton (Heir, retail chain) | added |
| `person_bill_gates` | 400 x 400 (1:1) | Bill Gates (Co-founder, software company) | added |
| `person_michael_bloomberg` | 400 x 400 (1:1) | Michael Bloomberg (Founder, financial data company) | placeholder |
| `person_mukesh_ambani` | 400 x 400 (1:1) | Mukesh Ambani (Chairman, industrial conglomerate) | added |
| `person_carlos_slim_helu` | 400 x 400 (1:1) | Carlos Slim Helú (Businessman, telecommunications) | added |
| `person_francoise_bettencourt_meyers` | 400 x 400 (1:1) | Françoise Bettencourt Meyers (Heir, cosmetics company) | placeholder |
| `person_ken_griffin` | 400 x 400 (1:1) | Ken Griffin (Founder, investment firm) | placeholder |
| `person_phil_knight` | 400 x 400 (1:1) | Phil Knight (Co-founder, sportswear company) | added |
| `person_mackenzie_scott` | 400 x 400 (1:1) | MacKenzie Scott (Author and philanthropist) | placeholder |
| `person_jack_ma` | 400 x 400 (1:1) | Jack Ma (Co-founder, e-commerce company) | added |
| `person_george_lucas` | 400 x 400 (1:1) | George Lucas (Filmmaker) | added |
| `person_mark_cuban` | 400 x 400 (1:1) | Mark Cuban (Entrepreneur and investor) | added |
| `person_steven_spielberg` | 400 x 400 (1:1) | Steven Spielberg (Film director) | placeholder |
| `person_michael_jordan` | 400 x 400 (1:1) | Michael Jordan (Former basketball player) | added |
| `person_oprah_winfrey` | 400 x 400 (1:1) | Oprah Winfrey (Media personality) | added |
| `person_jay_z` | 400 x 400 (1:1) | Jay-Z (Rapper and entrepreneur) | added |
| `person_kim_kardashian` | 400 x 400 (1:1) | Kim Kardashian (Media personality and entrepreneur) | added |
| `person_taylor_swift` | 400 x 400 (1:1) | Taylor Swift (Singer-songwriter) | added |
| `person_magic_johnson` | 400 x 400 (1:1) | Magic Johnson (Former basketball player and investor) | added |
| `person_rihanna` | 400 x 400 (1:1) | Rihanna (Singer and entrepreneur) | added |
| `person_cristiano_ronaldo` | 400 x 400 (1:1) | Cristiano Ronaldo (Footballer) | placeholder |
| `person_tiger_woods` | 400 x 400 (1:1) | Tiger Woods (Golfer) | added |
| `person_selena_gomez` | 400 x 400 (1:1) | Selena Gomez (Singer, actress and entrepreneur) | placeholder |
| `person_paul_mccartney` | 400 x 400 (1:1) | Paul McCartney (Musician) | added |
| `person_lebron_james` | 400 x 400 (1:1) | LeBron James (Basketball player) | added |
| `person_bruce_springsteen` | 400 x 400 (1:1) | Bruce Springsteen (Musician) | placeholder |
| `person_roger_federer` | 400 x 400 (1:1) | Roger Federer (Former tennis player) | added |
| `person_dwayne_johnson` | 400 x 400 (1:1) | Dwayne Johnson (Actor) | placeholder |
| `person_beyonce` | 400 x 400 (1:1) | Beyoncé (Singer) | added |
| `person_kylie_jenner` | 400 x 400 (1:1) | Kylie Jenner (Entrepreneur) | added |
| `person_tom_cruise` | 400 x 400 (1:1) | Tom Cruise (Actor) | placeholder |
| `person_lionel_messi` | 400 x 400 (1:1) | Lionel Messi (Footballer) | placeholder |
| `person_elton_john` | 400 x 400 (1:1) | Elton John (Musician) | added |
| `person_shaquille_o_neal` | 400 x 400 (1:1) | Shaquille O'Neal (Former basketball player and broadcaster) | added |
| `person_mrbeast` | 400 x 400 (1:1) | MrBeast (YouTuber) | added |
| `person_david_beckham` | 400 x 400 (1:1) | David Beckham (Former footballer) | added |
| `person_kevin_hart` | 400 x 400 (1:1) | Kevin Hart (Comedian and actor) | added |
| `person_tom_hanks` | 400 x 400 (1:1) | Tom Hanks (Actor) | added |
| `person_lewis_hamilton` | 400 x 400 (1:1) | Lewis Hamilton (Racing driver) | placeholder |
| `person_ryan_reynolds` | 400 x 400 (1:1) | Ryan Reynolds (Actor and entrepreneur) | added |
| `person_serena_williams` | 400 x 400 (1:1) | Serena Williams (Former tennis player) | added |
| `person_ed_sheeran` | 400 x 400 (1:1) | Ed Sheeran (Singer-songwriter) | added |
| `person_novak_djokovic` | 400 x 400 (1:1) | Novak Djokovic (Tennis player) | added |
| `person_gordon_ramsay` | 400 x 400 (1:1) | Gordon Ramsay (Chef and TV host) | added |
| `person_adele` | 400 x 400 (1:1) | Adele (Singer) | added |
| `person_stephen_curry` | 400 x 400 (1:1) | Stephen Curry (Basketball player) | added |
| `person_usain_bolt` | 400 x 400 (1:1) | Usain Bolt (Former sprinter) | placeholder |
| `person_emma_watson` | 400 x 400 (1:1) | Emma Watson (Actress) | placeholder |
| `person_pewdiepie` | 400 x 400 (1:1) | PewDiePie (YouTuber) | added |
| `person_tom_holland` | 400 x 400 (1:1) | Tom Holland (Actor) | added |
| `person_zendaya` | 400 x 400 (1:1) | Zendaya (Actress) | added |
| `person_millie_bobby_brown` | 400 x 400 (1:1) | Millie Bobby Brown (Actress) | added |
| `person_jenna_ortega` | 400 x 400 (1:1) | Jenna Ortega (Actress) | placeholder |
