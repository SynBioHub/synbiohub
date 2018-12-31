
var namespace = require('./namespace')
var summarizeTopLevel = require('./summarizeTopLevel')

var config = require('../config')

var URI = require('sboljs').URI

function summarizeActivity(activity,req,sbol,remote,graphUri) {

    if (activity instanceof URI)
	return {
            uri: activity + '',
	    id: activity + ''
	}

    var summary = {
        usages: summarizeUsages(activity,req),
        associations: summarizeAssociations(activity,req)
    }
    
    return Object.assign(summary,summarizeTopLevel(activity,req,sbol,remote,graphUri))
}

function summarizeUsages(activity,req) {
    var usages = []
    activity.usages.forEach((usage) => {
	var usageResult = {}
	usage.link()
	if (usage.entity.uri) {
            usageResult.defId = usage.entity.displayId
            usageResult.defName = usage.entity.name
            if (usage.entity.uri.toString().startsWith(config.get('databasePrefix'))) {
		usageResult.url = '/' + usage.entity.uri.toString().replace(config.get('databasePrefix'),'')
		if (usage.entity.uri.toString().startsWith(config.get('databasePrefix')+'user/') && req.url.toString().endsWith('/share')) {
		    usageResult.url += '/' + sha1('synbiohub_' + sha1(usage.entity.uri.toString()) + config.get('shareLinkSalt')) + '/share'
		}  
            } else { 
		usageResult.url = usage.entity.uri.toString()
	    }
	} else {
            usageResult.defId = usage.entity.toString()
            usageResult.defName = ''
            if (usage.entity.toString().startsWith(config.get('databasePrefix'))) {
		usageResult.url = '/' + usage.entity.toString().replace(config.get('databasePrefix'),'')
		if (usage.entity.toString().startsWith(config.get('databasePrefix')+'user/') && req.url.toString().endsWith('/share')) {
		    usageResult.url += '/' + sha1('synbiohub_' + sha1(usage.entity.toString()) + config.get('shareLinkSalt')) + '/share'
		}  
            } else { 
		usageResult.url = usage.entity.toString()
	    }
	}
	usageResult.typeStr = ''
	usage.roles.forEach((role) => {
	    usageResult.typeStr += role.toString().replace('http://sbols.org/v2#','') + ' '
	})
	usages.push(usageResult)
    })
    return usages
}

function summarizeAssociations(activity,req) {
    var associations = []
    activity.associations.forEach((association) => {
	var associationResult = {}
	associationResult.agent = {}
	associationResult.plan = {}
	association.link()
	if (association.agent.uri) {
            associationResult.agent.defId = association.agent.displayId
            associationResult.agent.defName = association.agent.name
            if (association.agent.uri.toString().startsWith(config.get('databasePrefix'))) {
		associationResult.agent.url = '/' + association.agent.uri.toString().replace(config.get('databasePrefix'),'')
		if (association.agent.uri.toString().startsWith(config.get('databasePrefix')+'user/') && req.url.toString().endsWith('/share')) {
		    associationResult.agent.url += '/' + sha1('synbiohub_' + sha1(association.agent.uri.toString()) + config.get('shareLinkSalt')) + '/share'
		}  
            } else { 
		associationResult.agent.url = association.agent.uri.toString()
	    }
	} else {
            associationResult.agent.defId = association.agent.toString()
            associationResult.agent.defName = ''
            if (association.agent.toString().startsWith(config.get('databasePrefix'))) {
		associationResult.agent.url = '/' + association.agent.toString().replace(config.get('databasePrefix'),'')
		if (association.agent.toString().startsWith(config.get('databasePrefix')+'user/') && req.url.toString().endsWith('/share')) {
		    associationResult.agent.url += '/' + sha1('synbiohub_' + sha1(association.agent.toString()) + config.get('shareLinkSalt')) + '/share'
		}  
            } else { 
		associationResult.agent.url = association.agent.toString()
	    }
	}
	associationResult.typeStr = ''
	association.roles.forEach((role) => {
	    associationResult.typeStr += role.toString().replace('http://sbols.org/v2#','') + ' '
	})
	if (association.plan) {
	    if (association.plan.uri) {
		associationResult.plan.defId = association.plan.displayId
		associationResult.plan.defName = association.plan.name
		if (association.plan.uri.toString().startsWith(config.get('databasePrefix'))) {
		    associationResult.plan.url = '/' + association.plan.uri.toString().replace(config.get('databasePrefix'),'')
		    if (association.plan.uri.toString().startsWith(config.get('databasePrefix')+'user/') && req.url.toString().endsWith('/share')) {
			associationResult.plan.url += '/' + sha1('synbiohub_' + sha1(association.plan.uri.toString()) + config.get('shareLinkSalt')) + '/share'
		    }  
		} else { 
		    associationResult.plan.url = association.plan.uri.toString()
		}
	    } else {
		associationResult.plan.defId = association.plan.toString()
		associationResult.plan.defName = ''
		if (association.plan.toString().startsWith(config.get('databasePrefix'))) {
		    associationResult.plan.url = '/' + association.plan.toString().replace(config.get('databasePrefix'),'')
		    if (association.plan.toString().startsWith(config.get('databasePrefix')+'user/') && req.url.toString().endsWith('/share')) {
			associationResult.plan.url += '/' + sha1('synbiohub_' + sha1(association.plan.toString()) + config.get('shareLinkSalt')) + '/share'
		    }  
		} else { 
		    associationResult.plan.url = association.plan.toString()
		}
	    }
	}
	associations.push(associationResult)
    })
    return associations
}

module.exports = summarizeActivity

