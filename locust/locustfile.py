from locust import HttpLocust, TaskSet, task
import json
import random

#Script to generate many Property Groups, Version Sets and Mappings, to use in Benchmark testing

headers = {'Content-Type': 'application/json', 'Authorization': 'Basic dXNlcjp1c2Vy'} 

def randomizeJson(jsonFile, prefixName, field):
    with open(jsonFile, 'r+') as f:
        json_data = json.load(f)
        i = random.randint(1,10000000)
	name = prefixName+"{}".format(i)
        json_data[field] = name
        f.seek(0)
        f.write(json.dumps(json_data))
        f.truncate()
	return name

def changeName(jsonFile, newName):
    with open(jsonFile, 'r+') as f:
        json_data = json.load(f)
	json_data['propertyGroupReferences'][0]['name'] = newName
        f.seek(0)
        f.write(json.dumps(json_data))
        f.truncate()

class UserBehavior(TaskSet):

    @task
    def main_task(self):
	#Create a Property Group
	pgName = randomizeJson('CreatePropertyGroup.json', 'pg', 'name')
        self.client.post("/v1/property-groups", open('CreatePropertyGroup.json').read(), headers=headers).content 

	#Create a VersionSet for this PropertyGroup
	vsName = randomizeJson('CreateVersionSet.json', 'vs', 'name')
	changeName('CreateVersionSet.json', pgName)
        self.client.post("/v1/version-sets", open('CreateVersionSet.json').read(), headers=headers).content 

	#Map this VersionSet
	payload = '{"name": "'+vsName+'","version": "1.0.0"}'
	self.client.put("/v1/mapping?application="+pgName, payload, headers=headers)

class WebsiteUser(HttpLocust):
    task_set = UserBehavior
    min_wait=3000
    max_wait = 5000
