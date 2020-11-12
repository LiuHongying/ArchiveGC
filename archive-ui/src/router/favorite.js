export const favoriteRouter = {
        meta: {
            requireAuth: true,
            permit: 1,
            title: 'favorite'
        },
        path: '/ShopingCart',
        name: '收藏夹',
        component: () => import('@/components/ShoppingCart.vue')
        
}