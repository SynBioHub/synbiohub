
var h
var mainLoop

var clickEvent

var state = {

    integrations: [],
    mode: 'list'

}

if(document.getElementById('sbh-data-integration')) {

    h = require('virtual-dom/virtual-hyperscript')
    clickEvent = require('value-event/click')

    const MainLoop = require('main-loop')
    const Delegator = require('dom-delegator')

    Delegator({
    })

    mainLoop = MainLoop(state, renderDataIntegrationView, {
        diff: require('virtual-dom/vtree/diff'),
        create: require('virtual-dom/vdom/create-element'),
        patch: require('virtual-dom/vdom/patch')
    })

    document.getElementById('sbh-data-integration').appendChild(mainLoop.target)
}

function update() {

    mainLoop.update(state)

}

function renderDataIntegrationView(state) {

    return ({
        'list': renderIntegrationList,
        'add': renderAddIntegration
    })[state.mode](state)


}

function renderIntegrationList(state) {

    var elems = []

    if(state.integrations.length === 0) {
        elems.push(
            h('br'),
            h('div', [
                h('h4', 'Add an integration step to begin'),
                h('br')
            ])

        )
    } else {
        elems.push(
            h('br'),
            h('div', [
                h('h4', 'The following integration steps will be executed:'),
                h('br')
            ])

        )

        elems = elems.concat(
            state.integrations.map((integration) => {
                return h('div', integration.name)
            })
        )

        elems.push(h('br'))
    }

    elems.push(
        h('button.btn.btn-primary', {
            'ev-click': clickEvent(clickAddStep)
        }, 'Add Step'),
        '  '
    )

    if(state.integrations.length === 0) { 
        elems.push(
            h('button.btn.disabled', 'Start Job')
        )
    } else {
        elems.push(
            h('button.btn.btn-success', 'Start Job')
        )
    }

    return h('div.sbh-data-integration-list', elems)

}

function renderAddIntegration(state) {

    return h('div', [
        h('span.fa.fa-arrow-left', {
            'ev-click': clickEvent(clickBack)
        }),
        h('table.table.table-striped', [
            h('thead', [
            ]),
            h('tbody', [
                integrations.map((integration) => {
                    return h('tr', {
                        'ev-click': clickEvent(clickIntegrationRow, { integration: integration })
                    }, [
                        h('td', integration.name),
                        h('td', integration.description),
                    ])
                })
            ])
        ])
    ])


}

function clickAddStep() {
    
    state.mode = 'add'

    update()

}

function clickBack() {

    state.mode = 'list'

    update()

}

function clickIntegrationRow(data) {

    const integration = data.integration

    state.integrations.push($.extend({}, integration))

    state.mode = 'list'

    update()


}





