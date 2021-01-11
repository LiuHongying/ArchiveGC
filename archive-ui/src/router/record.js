export const recordRouter =  [
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:"archivedelivery"
        },
        path: '/record/archivedelivery',
        name: '文档提交',
        component: () => import('@/views/record/ArchiveDelivery.vue')
    },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:"TC归档"
        },
        path: '/record/ReceivingTC',
        name: 'TC归档',
        component: () => import('@/views/record/receive/ReceivingTC.vue')
    },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:"OA归档"
        },
        path: '/record/ReceivingOA',
        name: 'OA归档',
        component: () => import('@/views/record/receive/ReceivingOA.vue')
    },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:"待作废文档"
        },
        path: '/record/ArchiveInvaliding',
        name: '待作废文档',
        component: () => import('@/views/record/ArchiveInvaliding.vue')
    },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:"作废库"
        },
        path: '/record/ArchiveInvalid',
        name: '作废库',
        component: () => import('@/views/record/ArchiveInvalid.vue')
    },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:"档案鉴定"
        },
        path: '/record/ArchivalAppraisal',
        name: '档案鉴定',
        component: () => import('@/views/record/ArchivalAppraisal.vue')
    },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:"档案销毁"
        },
        path: '/record/ArchivalDestruction',
        name: '档案销毁',
        component: () => import('@/views/record/ArchivalDestruction.vue')
    },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:'archiveReceive'
        },
        path: '/record/archivereceive',
        name: '档案接收',
        component: () => import('@/views/record/ArchiveReceive.vue')
    },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:'archiveArrange'
        },
        path: '/record/archivearrange',
        name: '档案整理',
        component: () => import('@/views/record/ArchiveArrange.vue')
    },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:'专题管理'
        },
        path: '/record/ThematicManagement',
        name: '专题管理',
        component: () => import('@/views/record/ThematicManagement.vue')
    },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:'档案专题'
        },
        path: '/record/ShowThematic',
        name: '档案专题',
        component: () => import('@/views/record/ShowThematic.vue')
    },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:'storageManager'
        },
        path: '/record/StorageRoom',
        component: () => import('@/views/record/StorageRoom.vue'),
        name: '库房管理'
    },
    {
        meta: {
          requireAuth: true,
          permit: 1,
          title: 'myWorkflow'
        },
        path: '/record/myborrow',
        name: '我的借阅',
        component: () => import('@/views/record/myborrow.vue'),
      },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:"archivetool"
        },
        path: '/record/archivetool',
        name: '档案工具',
        component: () => import('@/views/record/ArchiveTool.vue'),
        children:[
            {
                meta: {
                    requireAuth: true,
                    permit: 1,
                    title:"check4"
                },
                path: '/record/check4',
                name: '四性检查',
                component: () => import('@/views/record/Check4.vue')
            },
            {
                meta: {
                    requireAuth: true,
                    permit: 1,
                    title:"BatchUpdate"
                },
                path: '/record/batchupdate',
                name: '批量更新',
                component: () => import('@/views/record/BatchUpdate.vue')
            },
            {
                meta: {
                    requireAuth: true,
                    permit: 1,
                    title:'batchmount'
                },
                path: '/record/batchmount',
                name: '批量挂载',
                component: () => import('@/views/record/BatchMount.vue')
            },
            {
                meta: {
                    requireAuth: true,
                    permit: 1,
                    title:'batchserverimport'
                },
                path: '/record/batchserverimport',
                name: '服务器批量导入',
                component: () => import('@/views/record/BatchServerImport.vue')
            }
            
        ]
    },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title: 'archivehandover'
        },
        path: '/record/archivehandover',
        name: '移交入库',
        component: () => import('@/views/record/ArchiveHandOver.vue'),
    },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:'archiveoutgoing'
        },
        path: '/record/archiveoutgoing',
        name: '出入库管理',
        component: () => import('@/views/record/ArchiveOutgoing.vue'),
        children:[
            {
                meta: {
                    requireAuth: true,
                    permit: 1,
                    title:"archivependingout"
                },
                path: '/record/archiveoutgoing/archivependingout',
                name: '待出库',
                component: () => import('@/views/record/ArchivePendingOut.vue')
            },
            {
                meta: {
                  requireAuth: true,
                  permit: 1,
                  title:"archivegiveback"
                },
                path: '/record/archiveoutgoing/archivegiveback',
                name: '待归还',
                component: () => import('@/views/record/ArchivePendinggiveback.vue')
                
              },
            {
                meta: {
                    requireAuth: true,
                    permit: 1,
                    title:"archivependingin"
                },
                path: '/record/archiveoutgoing/archivependingin',
                name: '待入库',
                component: () => import('@/views/record/ArchivePendingIn.vue')
            },
            {
                meta: {
                    requireAuth: true,
                    permit: 1,
                    title:"archiveborrowcompleted"
                },
                path: '/record/archiveoutgoing/archiveborrowcompleted',
                name: '已完成',
                component: () => import('@/views/record/ArchiveBorrowcompleted.vue')
            }
        ]
    },
    {
        meta:{
            requireAuth: true,
            permit: 1,
            title:'archivebackup'
        },
        path: '/record/archivebackup',
        name: '光盘刻录',
        component: () => import('@/views/record/ArchiveBackup.vue')
    },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:'showdrawing'
        },
        path: '/record/showdrawing',
        name: '出入库管理',
        component: () => import('@/views/record/ShowDrawing.vue'),
        children:[
            {
                meta: {
                    requireAuth: true,
                    permit: 1,
                    title:"showdrawingpendingout"
                },
                path: '/record/showdrawing/showdrawingpendingout',
                name: '待晒图',
                component: () => import('@/views/record/ShowDrawingPendingOut.vue')
            },
            {
                meta: {
                    requireAuth: true,
                    permit: 1,
                    title:"showdrawingfinish"
                },
                path: '/record/showdrawing/showdrawingfinish',
                name: '已完成',
                component: () => import('@/views/record/ShowDrawingFinish.vue')
            }
        ]
    },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title:"showdrawingtask"
        },
        path: '/record/showdrawingtask',
        name: '出入库管理',
        component: () => import('@/views/record/ShowDrawingTask.vue'),
        children:[
            {
                meta: {
                    requireAuth: true,
                    permit: 1,
                    title:"showdrawingpendingouttask"
                },
                path: '/record/showdrawing/showdrawingpendingouttask',
                name: '待晒图',
                component: () => import('@/views/record/ShowDrawingPendingOut.vue')
            },
            {
                meta: {
                    requireAuth: true,
                    permit: 1,
                    title:"showdrawingfinishtask"
                },
                path: '/record/showdrawing/showdrawingfinishtask',
                name: '已完成',
                component: () => import('@/views/record/ShowDrawingFinish.vue')
            }
        ]
    }
    
]

// export const borrowCenter={
    // meta: {
    //     requireAuth: true,
    //     permit: 1
    // },
    // path: '/record/archiveoutgoing',
    // name: '出入库管理',
    // component: () => import('@/views/record/ArchiveOutgoing.vue'),
    // children: [
    //     {
    //         meta: {
    //             requireAuth: true,
    //             permit: 1
    //         },
    //         path: '/record/archiveoutgoing/archivepending',
    //         name: '待入库',
    //         component: () => import('@/views/record/ArchivePending.vue')
    //     }
    // ]
// }
