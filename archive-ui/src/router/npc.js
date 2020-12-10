export const npcRouter =  [
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:"CreateDoc"
        },
        path: '/npc/CreateDoc',
        name: '新建文档',
        component: () => import('@/views/npc/CreateDoc.vue')
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
]