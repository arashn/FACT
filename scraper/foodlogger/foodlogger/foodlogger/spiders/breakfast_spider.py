import scrapy

class BreakfastSpider(scrapy.Spider):
    name = "breakfast"

    def start_requests(self):
        urls = ['http://www.calorieking.com/foods/calories-in-einstein-bros-bagels_b-YmlkPTMyMA.html',
                'http://www.calorieking.com/foods/calories-in-au-bon-pain_b-YmlkPTUw.html',
                'http://www.calorieking.com/foods/calories-in-panda-express_b-YmlkPTc0Ng.html',
                'http://www.calorieking.com/foods/calories-in-jack-in-the-box_b-YmlkPTQ5MA.html',
                'http://www.calorieking.com/foods/calories-in-wendys_b-YmlkPTEwOTA.html',
                'http://www.calorieking.com/foods/calories-in-chick-fil-a_b-YmlkPTIwMw.html',
                'http://www.calorieking.com/foods/calories-in-taco-bell_b-YmlkPTk5NA.html',
                'http://www.calorieking.com/foods/calories-in-habit-burger-grill_b-YmlkPTEwMDAwMTM0.html',
                'http://www.calorieking.com/foods/calories-in-del-taco_b-YmlkPTI2OA.html']
        for url in urls:
            yield scrapy.Request(url=url, callback=self.parse)
    
    def parse(self, response):
        #breakfast = response.css('#breakfast')
        for href in response.css('#lunches ul li a::attr(href)'):
            yield response.follow(href, callback=self.parse_item)
        for href in response.css('#sandwiches-burgers ul li a::attr(href)'):
            yield response.follow(href, callback=self.parse_item)
        for href in response.css('#salads ul li a::attr(href)'):
            yield response.follow(href, callback=self.parse_item)
        for href in response.css('#menu-items ul li a::attr(href)'):
            yield response.follow(href, callback=self.parse_item)
        for href in response.css('#tacos ul li a::attr(href)'):
            yield response.follow(href, callback=self.parse_item)
        for href in response.css('#burritos ul li a::attr(href)'):
            yield response.follow(href, callback=self.parse_item)
        for href in response.css('#quesadillas ul li a::attr(href)'):
            yield response.follow(href, callback=self.parse_item)

    def parse_item(self, response):
        restaurant = response.css('#heading-food-cat-desc a::text').extract_first()
        item = response.css('#heading-food-cat-desc::text')[1].extract().strip()
        calories = response.css('#mCal::text').extract_first()
        print(restaurant, item, calories)
        with open('/home/anabili/restaurants.csv', 'a') as f:
            f.write(restaurant + ';' + item + ';2;' + calories + '\n')
