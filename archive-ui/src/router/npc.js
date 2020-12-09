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
]