export const favoriteRouter = [
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title: 'favorite'
        },
        path: '/ShopingCart',
        name: '收藏夹',
        component: () => import('@/components/form/ShopingCart.vue')
    },
    {
        meta: {
            requireAuth: true,
            permit: 1,
            title: 'subscription'
        },
        path: '/Subscription',
        name: '订阅',
        component: () => import('@/components/form/Subscription.vue')
    }
]