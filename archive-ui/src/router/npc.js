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
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:"effectiveDocuments"
        },
        path: '/npc/CreateWithTemplate',
        name: '文件创建',
        component: () => import('@/views/npc/CreateWithTemp.vue')
    },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:"effectiveDocuments"
        },
        path: '/npc/CreateWithTemplate2',
        name: '文件创建2',
        component: () => import('@/views/archivecd/CreateWithTemp.vue')
    },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:"distribute"
        },
        path: '/npc/distribute',
        name: '分发',
        component: () => import('@/views/archivecd/distribute/DistributeList.vue')
    },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:"haveReaded"
        },
        path: '/npc/readed',
        name: '分发',
        component: () => import('@/views/archivecd/distribute/DistributeListReaded.vue')
    },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:"Mysender"
        },
        path: '/npc/Mysender',
        name: '我的分发',
        component: () => import('@/views/archivecd/distribute/DistributeListSender.vue')
    },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:"Approving"
        },
        path: '/npc/Approve',
        name: '批示',
        component: () => import('@/views/archivecd/distribute/DistributeApproval.vue')
    },
    
]