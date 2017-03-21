
var h
var mainLoop

var clickEvent
var submitEvent

var state = {

    tasks: [],
    mode: 'list'

}

if(document.getElementById('sbh-data-integration')) {

    h = require('virtual-dom/virtual-hyperscript')
    clickEvent = require('value-event/click')
    submitEvent = require('value-event/submit')

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

    if(state.tasks.length === 0) {
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
            state.tasks.map((task) => {
                return h('div', task.name)
            })
        )

        elems.push(h('br'))
    }

    var formElems = []

    formElems.push(
        h('button.btn.btn-primary', {
            type: 'button',
            'ev-click': clickEvent(clickAddStep)
        }, 'Add Step'),
        '  '
    )

    if(state.tasks.length === 0) { 
        formElems.push(
            h('button.btn.disabled', { type: 'submit', disabled: 'disabled' }, 'Start Job')
        )
    } else {
        formElems.push(
            h('button.btn.btn-success', { type: 'submit' }, 'Start Job')
        )
    }

    if(typeof(graphUri) !== 'undefined') {
        formElems.push(h('input', {
            type: 'hidden',
            name: 'graphUri',
            value: graphUri
        }))
    }

    formElems.push(h('input', {
        type: 'hidden',
        name: 'inputUri',
        value: inputUri
    }))

    formElems.push(h('input', {
        type: 'hidden',
        name: 'tasks',
        value: JSON.stringify(state.tasks)
    }))

    elems.push(h('form', {

        method: 'post',
        action: 'integrate',

        //'ev-submit': submitEvent(submit)
        
    }, formElems))

    return h('div.sbh-di', elems)

}

function renderAddIntegration(state) {

    return h('div', [
        h('span.fa.fa-arrow-left.sbh-di-back', {
            'ev-click': clickEvent(clickBack)
        }),
        h('table.table.table-hover.table-striped.sbh-di-list', [
            h('thead', [
            ]),
            h('tbody', [
                tasks.map((task) => {
                    return h('tr.sbh-di-list-row', {
                        'ev-click': clickEvent(clickIntegrationRow, { task: task })
                    }, [
                        h('td', task.name),
                        h('td', task.description),
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

    const task = data.task

    state.tasks.push($.extend({}, task))

    state.mode = 'list'

    update()

}

function submit(data) {

    console.log('submit')

}





