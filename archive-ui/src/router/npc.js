export const npcRouter =  [
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:"CreateDrawingDoc"
        },
        path: '/npc/CreateDoc',
        name: '图纸文件创建',
        component: () => import('@/views/npc/CreateDoc.vue')
    },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:"DesignDocCreate"
        },
        path: '/npc/DesignDocCreate',
        name: '设计文件创建',
        component: () => import('@/views/npc/DesignDocCreate.vue')
    },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:"Drafts"
        },
        path: '/npc/Drafts',
        name: '草稿箱',
        component: () => import('@/views/npc/Drafts.vue')
    },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:"knowledgeBase"
        },
        path: '/npc/knowledgeBase',
        name: '知识库',
        component: () => import('@/views/npc/DocStorage.vue')
    },
]